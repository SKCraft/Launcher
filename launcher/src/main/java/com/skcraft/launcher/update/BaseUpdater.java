/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.update;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.skcraft.launcher.AssetsRoot;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.LauncherException;
import com.skcraft.launcher.dialog.FeatureSelectionDialog;
import com.skcraft.launcher.dialog.ProgressDialog;
import com.skcraft.launcher.install.*;
import com.skcraft.launcher.model.loader.LoaderManifest;
import com.skcraft.launcher.model.loader.LocalLoader;
import com.skcraft.launcher.model.minecraft.*;
import com.skcraft.launcher.model.modpack.DownloadableFile;
import com.skcraft.launcher.model.modpack.Feature;
import com.skcraft.launcher.model.modpack.Manifest;
import com.skcraft.launcher.model.modpack.ManifestEntry;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.util.Environment;
import com.skcraft.launcher.util.FileUtils;
import com.skcraft.launcher.util.HttpRequest;
import com.skcraft.launcher.util.SharedLocale;
import lombok.NonNull;
import lombok.extern.java.Log;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;

import static com.skcraft.launcher.LauncherUtils.checkInterrupted;
import static com.skcraft.launcher.LauncherUtils.concat;
import static com.skcraft.launcher.util.HttpRequest.url;

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

        // Make sure the temp dir exists
        installer.getTempDir().mkdirs();

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

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new FeatureSelectionDialog(ProgressDialog.getLastDialog(), features, BaseUpdater.this)
                            .setVisible(true);
                }
            });

            synchronized (this) {
                this.wait();
            }

            for (Feature feature : features) {
                featuresCache.getSelected().put(Strings.nullToEmpty(feature.getName()), feature.isSelected());
            }
        }

        // Download any extra processing files for each loader
        HashMap<String, LocalLoader> loaders = Maps.newHashMap();
        for (Map.Entry<String, LoaderManifest> entry : manifest.getLoaders().entrySet()) {
            HashMap<String, DownloadableFile.LocalFile> localFilesMap = Maps.newHashMap();

            for (DownloadableFile file : entry.getValue().getDownloadableFiles()) {
                if (file.getSide() != Side.CLIENT) continue;

                DownloadableFile.LocalFile localFile = file.download(installer, manifest);
                localFilesMap.put(localFile.getName(), localFile);
            }

            loaders.put(entry.getKey(), new LocalLoader(entry.getValue(), localFilesMap));
        }

        InstallExtras extras = new InstallExtras(contentDir, loaders);
        for (ManifestEntry entry : manifest.getTasks()) {
            entry.install(installer, currentLog, updateCache, extras);
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
                              @NonNull VersionManifest.Artifact artifact,
                              @NonNull File jarFile,
                              @NonNull URL url) throws InterruptedException {
        // If the JAR does not exist, install it
        if (!jarFile.exists()) {
            long size = artifact.getSize();
            if (size <= 0) size = JAR_SIZE_ESTIMATE;

            File tempFile = installer.getDownloader().download(url, "", size, jarFile.getName());
            installer.queue(new FileMover(tempFile, jarFile));
            if (artifact.getHash() != null) {
                installer.queue(new FileVerify(jarFile, jarFile.getName(), artifact.getHash()));
            }
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
                                    @NonNull Manifest manifest,
                                    @NonNull File librariesDir,
                                    @NonNull List<URL> sources) throws InterruptedException, IOException {
        VersionManifest versionManifest = manifest.getVersionManifest();

        Iterable<Library> allLibraries = versionManifest.getLibraries();
        for (LoaderManifest loader : manifest.getLoaders().values()) {
            allLibraries = Iterables.concat(allLibraries, loader.getLibraries());
        }

        for (Library library : allLibraries) {
            if (library.isGenerated()) continue; // Skip generated libraries.

            if (library.matches(environment)) {
                checkInterrupted();

                Library.Artifact artifact = library.getArtifact(environment);
                String path = artifact.getPath();

                long size = artifact.getSize();
                if (size <= 0) size = LIBRARY_SIZE_ESTIMATE;

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

                    File tempFile = installer.getDownloader().download(urls, "", size,
                            library.getName() + ".jar");
                    log.info("Fetching " + path + " from " + urls);
                    installer.queue(new FileMover(tempFile, targetFile));
                    if (artifact.getSha1() != null) {
                        installer.queue(new FileVerify(targetFile, library.getName(), artifact.getSha1()));
                    }
                }
            }
        }

        // Use our custom logging config depending on what the manifest specifies
        if (versionManifest.getLogging() != null && versionManifest.getLogging().getClient() != null) {
            VersionManifest.LoggingConfig config = versionManifest.getLogging().getClient();

            VersionManifest.Artifact file = config.getFile();
            File targetFile = new File(librariesDir, file.getId());
            InputStream embeddedConfig = Launcher.class.getResourceAsStream("logging/" + file.getId());

            if (embeddedConfig == null) {
                // No embedded config, just use whatever the server gives us
                File tempFile = installer.getDownloader().download(url(file.getUrl()), file.getHash(), file.getSize(), file.getId());

                log.info("Downloading logging config " + file.getId() + " from " + file.getUrl());
                installer.queue(new FileMover(tempFile, targetFile));
            } else if (!targetFile.exists() || FileUtils.getShaHash(targetFile).equals(file.getHash())) {
                // Use our embedded replacement

                Path tempFile = installer.getTempDir().toPath().resolve(file.getId());
                Files.copy(embeddedConfig, tempFile, StandardCopyOption.REPLACE_EXISTING);

                log.info("Substituting embedded logging config " + file.getId());
                installer.queue(new FileMover(tempFile.toFile(), targetFile));
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
