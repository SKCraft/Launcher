/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.minecraft;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;
import java.util.LinkedHashSet;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionManifest {

    private String id;
    private Date time;
    private Date releaseTime;
    private String assets;
    private String type;
    private String processArguments;
    private String minecraftArguments;
    private String mainClass;
    private int minimumLauncherVersion;
    private LinkedHashSet<Library> libraries;

    @JsonIgnore
    public String getAssetsIndex() {
        return getAssets() != null ? getAssets() : "legacy";
    }

}
