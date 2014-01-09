/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.update;

import com.skcraft.launcher.AssetsRoot;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.model.minecraft.Asset;
import com.skcraft.launcher.model.minecraft.AssetsIndex;
import com.skcraft.launcher.model.minecraft.Library;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import com.skcraft.launcher.util.Environment;
import com.skcraft.launcher.util.HttpRequest;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.skcraft.launcher.LauncherUtils.checkInterrupted;
import static com.skcraft.launcher.util.HttpRequest.url;

@Log
public class GameUpdater implements Runnable {

    private final Installer installer;
    private final Launcher launcher;
    private final VersionManifest versionManifest;
    private final URL librariesBaseURL;
    private Environment environment = Environment.getInstance();

    public GameUpdater(@NonNull Installer installer,
                       @NonNull Launcher launcher,
                       @NonNull VersionManifest versionManifest, URL librariesBaseURL) {
        this.installer = installer;
        this.launcher = launcher;
        this.versionManifest = versionManifest;
        this.librariesBaseURL = librariesBaseURL;
    }

    @Override
    public void run() {
        try {
            File librariesDir = launcher.getLibrariesDir();
            AssetsRoot assetsRoot = launcher.getAssets();
            File jarPath = launcher.getJarPath(versionManifest);

            URL jarURL = url(String.format(
                    launcher.getProperties().getProperty("jarUrl"), versionManifest.getId()));
            URL assetsIndexURL = url(String.format(
                    launcher.getProperties().getProperty("assetsIndexUrl"), versionManifest.getAssetsIndex()));

            // If the JAR does not exist, install it
            if (!jarPath.exists()) {
                List<File> targets = new ArrayList<File>();
                targets.add(jarPath);
                installer.submit(new FileDownloader(installer, jarURL, targets));
            }

            // Install libraries
            for (Library library : versionManifest.getLibraries()) {
                if (library.matches(environment)) {
                    URL url = library.getURL(launcher, environment, librariesBaseURL);
                    File file = new File(librariesDir, library.getPath(environment));

                    if (!file.exists()) {
                        List<File> targets = new ArrayList<File>();
                        targets.add(file);
                        installer.submit(new FileDownloader(installer, url, targets));
                    }

                    checkInterrupted();
                }
            }

            // Install assets
            AssetsIndex index = HttpRequest
                    .get(assetsIndexURL)
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .saveContent(assetsRoot.getIndexPath(versionManifest.getAssetsIndex()))
                    .asJson(AssetsIndex.class);

            for (Map.Entry<String, Asset> entry : index.getObjects().entrySet()) {
                String hash = entry.getValue().getHash();
                URL url = url(String.format(
                        launcher.getProperties().getProperty("assetUrl"), hash.subSequence(0, 2), hash));
                File path = assetsRoot.getObjectPath(entry.getValue());

                checkInterrupted();

                if (!path.exists()) {
                    List<File> targets = new ArrayList<File>();
                    targets.add(path);
                    installer.submit(new FileDownloader(installer, url, targets));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get resources", e);
        }
    }

    @Override
    public String toString() {
        return "GameUpdater{" +
                "versionManifest.id=" + versionManifest.getId() +
                ", environment=" + environment +
                '}';
    }
}
