/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import com.google.common.io.Files;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.model.minecraft.Asset;
import com.skcraft.launcher.model.minecraft.AssetsIndex;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import com.skcraft.launcher.persistence.Persistence;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
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
    private final File dir;

    /**
     * Create a new instance.
     *
     * @param dir the directory to the assets folder
     */
    public AssetsRoot(@NonNull File dir) {
        this.dir = dir;
    }

    /**
     * Get the path to the index .json file for a version manfiest.
     *
     * @param versionManifest the version manifest
     * @return the file, which may not exist
     */
    public File getIndexPath(VersionManifest versionManifest) {
        return new File(dir, "indexes/" + versionManifest.getAssetsIndex() + ".json");
    }

    /**
     * Get the local path for a given asset.
     *
     * @param asset the asset
     * @return the file, which may not exist
     */
    public File getObjectPath(Asset asset) {
        String hash = asset.getHash();
        return new File(dir, "objects/" + hash.substring(0, 2) + "/" + hash);
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
    public AssetsTreeBuilder createAssetsBuilder(@NonNull VersionManifest versionManifest) throws LauncherException {
        String indexId = versionManifest.getAssetsIndex();
        File path = getIndexPath(versionManifest);
        AssetsIndex index = Persistence.read(path, AssetsIndex.class, true);
        if (index == null || index.getObjects() == null) {
            throw new LauncherException("Missing index at " + path, tr("assets.missingIndex", path.getAbsolutePath()));
        }
        File treeDir = new File(dir, "virtual/" + indexId);
        treeDir.mkdirs();
        return new AssetsTreeBuilder(index, treeDir);
    }

    public class AssetsTreeBuilder implements ProgressObservable {
        private final AssetsIndex index;
        private final File destDir;
        private final int count;
        private int processed = 0;

        public AssetsTreeBuilder(AssetsIndex index, File destDir) {
            this.index = index;
            this.destDir = destDir;
            count = index.getObjects().size();
        }

        public File build() throws IOException, LauncherException {
            AssetsRoot.log.info("Building asset virtual tree at '" + destDir.getAbsolutePath() + "'...");

            for (Map.Entry<String, Asset> entry : index.getObjects().entrySet()) {
                File objectPath = getObjectPath(entry.getValue());
                File virtualPath = new File(destDir, entry.getKey());
                virtualPath.getParentFile().mkdirs();
                if (!virtualPath.exists()) {
                    log.log(Level.INFO, "Copying {0} to {1}...", new Object[] {
                            objectPath.getAbsolutePath(), virtualPath.getAbsolutePath()});

                    if (!objectPath.exists()) {
                        String message = tr("assets.missingObject", objectPath.getAbsolutePath());
                        throw new LauncherException("Missing object " + objectPath.getAbsolutePath(), message);
                    }

                    Files.copy(objectPath, virtualPath);
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
