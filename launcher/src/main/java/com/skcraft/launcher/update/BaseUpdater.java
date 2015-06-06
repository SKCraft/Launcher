/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.update;

import com.google.common.base.Strings;
import com.skcraft.launcher.AssetsRoot;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.LauncherException;
import com.skcraft.launcher.dialog.FeatureSelectionDialog;
import com.skcraft.launcher.dialog.ProgressDialog;
import com.skcraft.launcher.install.*;
import com.skcraft.launcher.model.minecraft.Asset;
import com.skcraft.launcher.model.minecraft.AssetsIndex;
import com.skcraft.launcher.model.minecraft.Library;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import com.skcraft.launcher.model.modpack.Feature;
import com.skcraft.launcher.model.modpack.Manifest;
import com.skcraft.launcher.model.modpack.ManifestEntry;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.util.Environment;
import com.skcraft.launcher.util.HttpRequest;
import com.skcraft.launcher.util.SharedLocale;
import lombok.NonNull;
import lombok.extern.java.Log;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

import static com.skcraft.launcher.LauncherUtils.checkInterrupted;
import static com.skcraft.launcher.LauncherUtils.concat;

/**
 * The base implementation of the various routines involved in downloading
 * and updating Minecraft (including the launcher's modpacks), such as asset
 * downloading, .jar downloading, and so on.
 * </p>
 * Updating actually starts in {@link com.skcraft.launcher.update.Updater},
 * which is the update worker. This class exists to allow updaters that don't
 * use the launcher's default modpack format to reuse these update
 * routines. (It also makes the size of the <code>Updater</code> class smaller.)
 */
@Log
public abstract class BaseUpdater {

    private static final long JAR_SIZE_ESTIMATE = 5 * 1024 * 1024;
    private static final long LIBRARY_SIZE_ESTIMATE = 3 * 1024 * 1024;

    private final Launcher launcher;
    private final Environment environment = Environment.getInstance();
    private final List<Runnable> executeOnCompletion = new ArrayList<Runnable>();

    protected BaseUpdater(@NonNull Launcher launcher) {
        this.launcher = launcher;
    }

    protected void complete() {
        for (Runnable runnable : executeOnCompletion) {
            runnable.run();
        }
    }

    protected Manifest installPackage(@NonNull Installer installer, @NonNull Instance instance) throws Exception {
        final File contentDir = instance.getContentDir();
        final File logPath = new File(instance.getDir(), "install_log.json");
        final File cachePath = new File(instance.getDir(), "update_cache.json");
        final File featuresPath = new File(instance.getDir(), "features.json");

        final InstallLog previousLog = Persistence.read(logPath, InstallLog.class);
        final InstallLog currentLog = new InstallLog();
        currentLog.setBaseDir(contentDir);
        final UpdateCache updateCache = Persistence.read(cachePath, UpdateCache.class);
        final FeatureCache featuresCache = Persistence.read(featuresPath, FeatureCache.class);

        Manifest manifest = HttpRequest
                .get(instance.getManifestURL())
                .execute()
                .expectResponseCode(200)
                .returnContent()
                .saveContent(instance.getManifestPath())
                .asJson(Manifest.class);

        if (manifest.getMinimumVersion() > Launcher.PROTOCOL_VERSION) {
            throw new LauncherException("Update required", SharedLocale.tr("errors.updateRequiredError"));
        }

        if (manifest.getBaseUrl() == null) {
            manifest.setBaseUrl(instance.getManifestURL());
        }

        final List<Feature> features = manifest.getFeatures();
        if (!features.isEmpty()) {
            for (Feature feature : features) {
                Boolean last = featuresCache.getSelected().get(feature.getName());
                if (last != null) {
                    feature.setSelected(last);
                }
            }

            Collections.sort(features);

            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    new FeatureSelectionDialog(ProgressDialog.getLastDialog(), features).setVisible(true);
                }
            });

            for (Feature feature : features) {
                featuresCache.getSelected().put(Strings.nullToEmpty(feature.getName()), feature.isSelected());
            }
        }

        for (ManifestEntry entry : manifest.getTasks()) {
            entry.install(installer, currentLog, updateCache, contentDir);
        }

        executeOnCompletion.add(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, Set<String>> entry : previousLog.getEntrySet()) {
                    for (String path : entry.getValue()) {
                        if (!currentLog.has(path)) {
                            new File(contentDir, path).delete();
                        }
                    }
                }

                writeDataFile(logPath, currentLog);
                writeDataFile(cachePath, updateCache);
                writeDataFile(featuresPath, featuresCache);
            }
        });

        return manifest;
    }

    protected void installJar(@NonNull Installer installer,
                              @NonNull File jarFile,
                              @NonNull URL url) throws InterruptedException {
        // If the JAR does not exist, install it
        if (!jarFile.exists()) {
            List<File> targets = new ArrayList<File>();

            File tempFile = installer.getDownloader().download(url, "", JAR_SIZE_ESTIMATE, jarFile.getName());
            installer.queue(new FileMover(tempFile, jarFile));
            log.info("Installing " + jarFile.getName() + " from " + url);
        }
    }

    protected void installAssets(@NonNull Installer installer,
                                 @NonNull VersionManifest versionManifest,
                                 @NonNull URL indexUrl,
                                 @NonNull List<URL> sources) throws IOException, InterruptedException {
        AssetsRoot assetsRoot = launcher.getAssets();

        AssetsIndex index = HttpRequest
                .get(indexUrl)
                .execute()
                .expectResponseCode(200)
                .returnContent()
                .saveContent(assetsRoot.getIndexPath(versionManifest))
                .asJson(AssetsIndex.class);

        // Keep track of duplicates
        Set<String> downloading = new HashSet<String>();

        for (Map.Entry<String, Asset> entry : index.getObjects().entrySet()) {
            checkInterrupted();

            String hash = entry.getValue().getHash();
            String path = String.format("%s/%s", hash.subSequence(0, 2), hash);
            File targetFile = assetsRoot.getObjectPath(entry.getValue());

            if (!targetFile.exists() && !downloading.contains(path)) {
                List<URL> urls = new ArrayList<URL>();
                for (URL sourceUrl : sources) {
                    try {
                        urls.add(concat(sourceUrl, path));
                    } catch (MalformedURLException e) {
                        log.log(Level.WARNING, "Bad source URL for library: " + sourceUrl);
                    }
                }

                File tempFile = installer.getDownloader().download(
                        urls, "", entry.getValue().getSize(), entry.getKey());
                installer.queue(new FileMover(tempFile, targetFile));
                log.info("Fetching " + path + " from " + urls);
                downloading.add(path);
            }
        }
    }

    protected void installLibraries(@NonNull Installer installer,
                                    @NonNull VersionManifest versionManifest,
                                    @NonNull File librariesDir,
                                    @NonNull List<URL> sources) throws InterruptedException {

        for (Library library : versionManifest.getLibraries()) {
            if (library.matches(environment)) {
                checkInterrupted();

                String path = library.getPath(environment);
                File targetFile = new File(librariesDir, path);

                if (!targetFile.exists()) {
                    List<URL> urls = new ArrayList<URL>();
                    for (URL sourceUrl : sources) {
                        try {
                            urls.add(concat(sourceUrl, path));
                        } catch (MalformedURLException e) {
                            log.log(Level.WARNING, "Bad source URL for library: " + sourceUrl);
                        }
                    }

                    File tempFile = installer.getDownloader().download(urls, "", LIBRARY_SIZE_ESTIMATE,
                            library.getName() + ".jar");
                    installer.queue(new FileMover( tempFile, targetFile));
                    log.info("Fetching " + path + " from " + urls);
                }
            }
        }
    }

    private static void writeDataFile(File path, Object object) {
        try {
            Persistence.write(path, object);
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to write to " + path.getAbsolutePath() +
                    " for object " + object.getClass().getCanonicalName(), e);
        }
    }

}
