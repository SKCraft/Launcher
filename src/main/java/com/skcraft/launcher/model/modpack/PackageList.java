/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.modpack;

import lombok.Data;

import java.util.List;

@Data
public class PackageList {

    private int minimumVersion;
    public List<ManifestInfo> packages;

}
