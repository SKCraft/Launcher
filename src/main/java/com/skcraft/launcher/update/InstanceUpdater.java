/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.update;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import com.skcraft.launcher.model.modpack.Manifest;
import com.skcraft.launcher.util.HttpRequest;
import com.skcraft.launcher.persistence.Persistence;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import static com.skcraft.launcher.util.HttpRequest.url;

@Log
public class InstanceUpdater implements Callable<Instance>, ProgressObservable {

    private final Launcher launcher;
    private final Instance instance;
    private final ListeningExecutorService executor =
            MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(6));
    private Installer currentInstaller;

    public InstanceUpdater(@NonNull Launcher launcher, @NonNull Instance instance) {
        this.launcher = launcher;
        this.instance = instance;
    }

    private URL getVersionManifestURL(String version) {
        return url(String.format(launcher.getProperties().getProperty("versionManifestUrl"), version));
    }

    @Override
    public Instance call() throws Exception {
        try {
            log.info("Checking for an update for '" + instance.getName() + "'...");
            if (instance.getManifestURL() == null) {
                log.log(Level.INFO,
                        "No URL set for {0}, so it can't be updated (the modpack may be removed from the server)",
                        new Object[] { instance });
            } else if (instance.isUpdatePending() || !instance.isInstalled()) {
                log.log(Level.INFO, "Updating {0}...", new Object[]{instance});
                update(instance);
            } else {
                log.log(Level.INFO, "No update found for {0}.", new Object[] { instance });
            }

            return instance;
        } finally {
            executor.shutdownNow();
        }
    }

    private void update(Instance instance) throws IOException, InterruptedException, ExecutionException {
        try {
            instance.setLocal(true);
            Persistence.commitAndForget(instance);

            Installer installer = new Installer(executor,
                    launcher.getInstallerDir(),
                    instance.getDir(),
                    instance.getContentDir());
            currentInstaller = installer;

            Manifest manifest = HttpRequest
                    .get(instance.getManifestURL())
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .saveContent(instance.getManifestPath())
                    .asJson(Manifest.class);
            if (manifest.getBaseUrl() == null) {
                manifest.setBaseUrl(instance.getManifestURL());
            }
            manifest.setInstaller(installer);

            installer.submitAll(manifest.getTasks());
            installer.awaitCompletion();
            installer.commit();

            URL url = getVersionManifestURL(manifest.getGameVersion());
            log.log(Level.INFO, instance.getName() + ": Fetching version manifest from " + url + "...");

            VersionManifest versionManifest = manifest.getVersionManifest();

            if (versionManifest != null) {
                Persistence.write(instance.getVersionManifestPath(), versionManifest);
            } else {
                // The manifest doesn't come with its own version manifest, so let's download the one for the given
                // version of Minecraft
                versionManifest = HttpRequest
                        .get(url)
                        .execute()
                        .expectResponseCode(200)
                        .returnContent()
                        .saveContent(instance.getVersionManifestPath())
                        .asJson(VersionManifest.class);
            }

            installer = new Installer(executor,
                    launcher.getInstallerDir(),
                    launcher.getCommonDataDir(),
                    launcher.getCommonDataDir());
            currentInstaller = installer;

            log.log(Level.INFO, instance.getName() + ": Enumerating common data files...");
            installer.submit(new GameUpdater(installer, launcher, versionManifest, manifest.getLibrariesURL()));
            installer.awaitCompletion();

            instance.setVersion(manifest.getVersion());
            instance.setUpdatePending(false);
            instance.setInstalled(true);
            instance.setLocal(true);
            Persistence.commitAndForget(instance);

            log.log(Level.INFO, instance.getName() + " has been updated to version " + manifest.getVersion() + ".");
        } finally {
            currentInstaller = null;
        }
    }

    @Override
    public double getProgress() {
        return -1;
    }

    @Override
    public String toString() {
        Installer installer = currentInstaller;
        if (installer != null) {
            return installer.toString();
        } else {
            return "...";
        }
    }
}
