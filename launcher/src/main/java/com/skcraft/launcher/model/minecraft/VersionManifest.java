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
import java.util.HashMap;
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
    private Arguments arguments;
    private String mainClass;
    private int minimumLauncherVersion;
    private LinkedHashSet<Library> libraries;
    private HashMap<String, String> assetIndex;

    @JsonIgnore
    public String getAssetsIndex() {
        return getAssets() != null ? getAssets() : "legacy";
    }
    @JsonIgnore
    public String getNewMinecraftArguments() {
        return getMinecraftArguments() != null ? getMinecraftArguments() : getNewArguments();
    }
    @JsonIgnore
    private String getNewArguments(){
        String result = "";
        if(getArguments()!=null)
        for(Object obj:getArguments().getGame()){
            if(obj instanceof String) {
                result += ((String)obj + " ");
            }
        }
        return result;
    }

}
