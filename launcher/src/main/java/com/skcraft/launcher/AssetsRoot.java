/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.model.minecraft.Asset;
import com.skcraft.launcher.model.minecraft.AssetsIndex;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import com.skcraft.launcher.persistence.Persistence;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;

import static com.skcraft.launcher.util.SharedLocale.tr;

/**
 * Represents a directory that stores assets for Minecraft. The class has
 * various methods that abstract operations involving the assets (such
 * as getting the path to a certain object).
 */
@Log
public class AssetsRoot {
    @Getter
    private final Path dir;

    /**
     * Create a new instance.
     *
     * @param dir the directory to the assets folder
     */
    public AssetsRoot(@NonNull Path dir) {
        this.dir = dir;
    }

    /**
     * Get the path to the index .json file for a version manfiest.
     *
     * @param versionManifest the version manifest
     * @return the file, which may not exist
     */
    public Path getIndexPath(VersionManifest versionManifest) {
        return dir.resolve("indexes").resolve(versionManifest.getAssetId() + ".json");
    }

    /**
     * Get the local path for a given asset.
     *
     * @param asset the asset
     * @return the file, which may not exist
     */
    public Path getObjectPath(Asset asset) {
        String hash = asset.getHash();
        return dir.resolve("objects").resolve(hash.substring(0, 2)).resolve(hash);
    }

    /**
     * Create an instance of the assets tree builder, which copies the indexed
     * assets (identified by hashes) into a directory where the assets
     * have been renamed and moved to their real names and locations
     * (i.e. sounds/whatever.ogg).
     *
     * @param versionManifest the version manifest
     * @return the builder
     * @throws LauncherException
     */
    public AssetsTreeBuilder createAssetsBuilder(@NonNull VersionManifest versionManifest) throws LauncherException, IOException {
        var indexId = versionManifest.getAssetId();
        var path = getIndexPath(versionManifest);
        AssetsIndex index = Persistence.read(path.toFile(), AssetsIndex.class, true);
        if (index == null || index.getObjects() == null) {
            throw new LauncherException("Missing index at " + path, tr("assets.missingIndex", path.toAbsolutePath()));
        }

        var treeDir = dir.resolve("virtual").resolve(indexId);
        Files.createDirectories(treeDir);
        return new AssetsTreeBuilder(index, treeDir);
    }

    public class AssetsTreeBuilder implements ProgressObservable {
        private final AssetsIndex index;
        private final Path destDir;
        private final int count;
        private int processed = 0;

        public AssetsTreeBuilder(AssetsIndex index, Path destDir) {
            this.index = index;
            this.destDir = destDir;
            count = index.getObjects().size();
        }

        public Path build() throws IOException, LauncherException {
            AssetsRoot.log.info("Building asset virtual tree at '" + destDir.toAbsolutePath() + "'...");

            boolean supportsLinks = true;
            for (Map.Entry<String, Asset> entry : index.getObjects().entrySet()) {
                var objectPath = getObjectPath(entry.getValue());
                var virtualPath = destDir.resolve(entry.getKey());

                if (!Files.exists(virtualPath)) {
                    Files.createDirectories(virtualPath.getParent());
                    log.log(Level.INFO, "Copying {0} to {1}...", new Object[] {
                            objectPath.toString(), virtualPath.toString()});

                    if (!Files.exists(objectPath)) {
                        String message = tr("assets.missingObject", objectPath.toAbsolutePath());
                        throw new LauncherException("Missing object " + objectPath.toAbsolutePath(), message);
                    }

                    if (supportsLinks) {
                        try {
                            Files.createLink(virtualPath, objectPath);
                        } catch (UnsupportedOperationException e) {
                            supportsLinks = false;
                        }
                    }

                    if (!supportsLinks) {
                        Files.copy(objectPath, virtualPath);
                    }
                }
                processed++;
            }

            return destDir;
        }

        @Override
        public double getProgress() {
            if (count == 0) {
                return -1;
            } else {
                return processed / (double) count;
            }
        }

        @Override
        public String getStatus() {
            if (count == 0) {
                return tr("assets.expanding1", count, count - processed);
            } else {
                return tr("assets.expandingN", count, count - processed);
            }
        }
    }

}
