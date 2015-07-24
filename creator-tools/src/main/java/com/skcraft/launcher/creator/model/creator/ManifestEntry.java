/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.model.creator;

import com.google.common.collect.Lists;
import com.skcraft.launcher.model.modpack.ManifestInfo;
import lombok.Data;

import java.util.List;

@Data
public class ManifestEntry implements Comparable<ManifestEntry> {

    private boolean selected = false;
    private ManifestInfo manifestInfo;
    private List<String> gameKeys = Lists.newArrayList();

    @Override
    public int compareTo(ManifestEntry o) {
        return manifestInfo.compareTo(o.getManifestInfo());
    }

}
