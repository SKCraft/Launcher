/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.minecraft;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.skcraft.launcher.AssetsRoot;
import lombok.Data;
import lombok.NonNull;

import java.io.File;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssetsIndex {

    private boolean virtual;
    private Map<String, Asset> objects;

    public File getObjectPath(@NonNull AssetsRoot assetsRoot, @NonNull String name) {
        Asset asset = objects.get(name);
        if (asset != null) {
            return assetsRoot.getObjectPath(asset);
        } else {
            return null;
        }
    }

}
