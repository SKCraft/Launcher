/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import com.google.common.io.Files;
import com.skcraft.launcher.model.minecraft.Asset;
import com.skcraft.launcher.model.minecraft.AssetsIndex;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import com.skcraft.launcher.persistence.Persistence;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

@Log
public class AssetsRoot {

    @Getter
    private final File dir;

    public AssetsRoot(File dir) {
        this.dir = dir;
    }

    public File getIndexPath(VersionManifest versionManifest) {
        return new File(dir, "indexes/" + versionManifest.getAssetsIndex() + ".json");
    }

    public File getObjectPath(Asset asset) {
        String hash = asset.getHash();
        return new File(dir, "objects/" + hash.substring(0, 2) + "/" + hash);
    }

    public File buildAssetTree(VersionManifest versionManifest) throws IOException {
        String indexId = versionManifest.getAssetsIndex();

        log.info("Building asset virtual tree for '" + versionManifest + "'");

        AssetsIndex index = Persistence.read(getIndexPath(versionManifest), AssetsIndex.class);
        File treeDir = new File(dir, "virtual/" + indexId);
        treeDir.mkdirs();

        for (Map.Entry<String, Asset> entry : index.getObjects().entrySet()) {
            File objectPath = getObjectPath(entry.getValue());
            File virtualPath = new File(treeDir, entry.getKey());
            virtualPath.getParentFile().mkdirs();
            if (!virtualPath.exists()) {
                log.log(Level.INFO, "Copying {0} to {1}...", new Object[] {
                        objectPath.getAbsolutePath(), virtualPath.getAbsolutePath()});
                Files.copy(objectPath, virtualPath);
            }
        }

        return treeDir;
    }

}
