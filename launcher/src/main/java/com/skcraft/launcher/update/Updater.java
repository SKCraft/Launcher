/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.update;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skcraft.concurrency.DefaultProgress;
import com.skcraft.concurrency.ProgressFilter;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.LauncherException;
import com.skcraft.launcher.install.Installer;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import com.skcraft.launcher.model.modpack.Manifest;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.util.HttpRequest;
import com.skcraft.launcher.util.SharedLocale;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import static com.skcraft.launcher.util.HttpRequest.url;

@Log
public class Updater extends BaseUpdater implements Callable<Instance>, ProgressObservable {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Installer installer;
    private final Launcher launcher;
    private final Instance instance;

    @Getter @Setter
    private boolean online;

    private List<URL> librarySources = new ArrayList<URL>();
    private List<URL> assetsSources = new ArrayList<URL>();

    private ProgressObservable progress = new DefaultProgress(-1, SharedLocale.tr("instanceUpdater.preparingUpdate"));

    public Updater(@NonNull Launcher launcher, @NonNull Instance instance) {
        super(launcher);

        this.installer = new Installer(launcher.getInstallerDir());
        this.launcher = launcher;
        this.instance = instance;

        librarySources.add(launcher.propUrl("librariesSource"));
        assetsSources.add(launcher.propUrl("assetsSource"));
    }

    @Override
    public Instance call() throws Exception {
        log.info("Checking for an update for '" + instance.getName() + "'...");

        // Force the directory to be created
        instance.getContentDir();

        boolean updateRequired = !instance.isInstalled();
        boolean updateDesired = (instance.isUpdatePending() || updateRequired);
        boolean updateCapable = (instance.getManifestURL() != null);

        if (!online && updateRequired) {
            log.info("Can't update " + instance.getTitle() + " because offline");
            String message = SharedLocale.tr("updater.updateRequiredButOffline");
            throw new LauncherException("Update required but currently offline", message);
        }

        if (updateDesired && !updateCapable) {
            if (updateRequired) {
                log.info("Update required for " + instance.getTitle() + " but there is no manifest");
                String message = SharedLocale.tr("updater.updateRequiredButNoManifest");
                throw new LauncherException("Update required but no manifest", message);
            } else {
                log.info("Can't update " + instance.getTitle() + ", but update is not required");
                return instance; // Can't update
            }
        }

        if (updateDesired) {
            log.info("Updating " + instance.getTitle() + "...");
            update(instance);
        } else {
            log.info("No update found for " + instance.getTitle());
        }

        return instance;
    }

    private VersionManifest readVersionManifest(Manifest manifest) throws IOException, InterruptedException {
        // Check whether the package manifest contains an embedded version manifest,
        // otherwise we'll have to download the one for the given Minecraft version
        VersionManifest version = manifest.getVersionManifest();
        if (version != null) {
            mapper.writeValue(instance.getVersionPath(), version);
            return version;
        } else {
            URL url = url(String.format(
                    launcher.getProperties().getProperty("versionManifestUrl"),
                    manifest.getGameVersion()));

            return HttpRequest
                    .get(url)
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .saveContent(instance.getVersionPath())
                    .asJson(VersionManifest.class);
        }
    }

    /**
     * Update the given instance.
     *
     * @param instance the instance
     * @throws IOException thrown on I/O error
     * @throws InterruptedException thrown on interruption
     * @throws ExecutionException thrown on execution error
     */
    protected void update(Instance instance) throws Exception {
        // Mark this instance as local
        instance.setLocal(true);
        Persistence.commitAndForget(instance);

        // Read manifest
        log.info("Reading package manifest...");
        progress = new DefaultProgress(-1, SharedLocale.tr("instanceUpdater.readingManifest"));
        Manifest manifest = installPackage(installer, instance);

        // Update instance from manifest
        manifest.update(instance);

        // Read version manifest
        log.info("Reading version manifest...");
        progress = new DefaultProgress(-1, SharedLocale.tr("instanceUpdater.readingVersion"));
        VersionManifest version = readVersionManifest(manifest);

        progress = new DefaultProgress(-1, SharedLocale.tr("instanceUpdater.buildingDownloadList"));

        // Install the .jar
        File jarPath = launcher.getJarPath(version);
        URL jarSource = launcher.propUrl("jarUrl", version.getId());
        log.info("JAR at " + jarPath.getAbsolutePath() + ", fetched from " + jarSource);
        installJar(installer, jarPath, jarSource);

        // Download libraries
        log.info("Enumerating libraries to download...");

        URL url = manifest.getLibrariesUrl();
        if (url != null) {
            log.info("Added library source: " + url);
            librarySources.add(url);
        }

        progress = new DefaultProgress(-1, SharedLocale.tr("instanceUpdater.collectingLibraries"));
        installLibraries(installer, version, launcher.getLibrariesDir(), librarySources);

        // Download assets
        log.info("Enumerating assets to download...");
        progress = new DefaultProgress(-1, SharedLocale.tr("instanceUpdater.collectingAssets"));
        installAssets(installer, version, launcher.propUrl("assetsIndexUrl", version.getAssetsIndex()), assetsSources);

        log.info("Executing download phase...");
        progress = ProgressFilter.between(installer.getDownloader(), 0, 0.98);
        installer.download();

        log.info("Executing install phase...");
        progress = ProgressFilter.between(installer, 0.98, 1);
        installer.execute();

        log.info("Completing...");
        complete();

        // Update the instance's information
        log.info("Writing instance information...");
        instance.setVersion(manifest.getVersion());
        instance.setUpdatePending(false);
        instance.setInstalled(true);
        instance.setLocal(true);
        Persistence.commitAndForget(instance);

        log.log(Level.INFO, instance.getName() +
                " has been updated to version " + manifest.getVersion() + ".");
    }

    @Override
    public double getProgress() {
        return progress.getProgress();
    }

    @Override
    public String getStatus() {
        return progress.getStatus();
    }


}
