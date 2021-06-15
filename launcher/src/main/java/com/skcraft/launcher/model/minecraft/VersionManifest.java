/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.minecraft;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Splitter;
import lombok.Data;

import java.util.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionManifest {

    private String id;
    private Date time;
    private Date releaseTime;
    private String assets;
    private AssetIndex assetIndex;
    private String type;
    private MinecraftArguments arguments;
    private String mainClass;
    private int minimumLauncherVersion;
    private LinkedHashSet<Library> libraries;
    private JavaVersion javaVersion;
    private Map<String, Artifact> downloads = new HashMap<String, Artifact>();

    public String getAssetId() {
        return getAssetIndex() != null
                ? getAssetIndex().getId()
                : "legacy";
    }

    public Library findLibrary(String name) {
        for (Library library : getLibraries()) {
            if (library.getName().equals(name)) {
                return library;
            }
        }

        return null;
    }

    public void setMinecraftArguments(String minecraftArguments) {
        MinecraftArguments result = new MinecraftArguments();
        result.setGameArguments(new ArrayList<GameArgument>());
        result.setJvmArguments(new ArrayList<GameArgument>());

        for (String arg : Splitter.on(' ').split(minecraftArguments)) {
            result.getGameArguments().add(new GameArgument(arg));
        }

        setArguments(result);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Artifact {
        private String url;
        private int size;

        @JsonProperty("sha1")
        private String hash;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssetIndex {
        private String id;
        private String url;
    }
}
