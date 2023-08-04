/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.loader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Splitter;
import com.skcraft.launcher.model.minecraft.GameArgument;
import com.skcraft.launcher.model.minecraft.Library;
import com.skcraft.launcher.model.minecraft.MinecraftArguments;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionInfo {
    private String id;
    private MinecraftArguments arguments;
    private String mainClass;
    private List<Library> libraries;
    private SidedData<VersionManifest.LoggingConfig> logging;

    @JsonIgnore private transient boolean overridingArguments;

    public void setMinecraftArguments(String argumentString) {
        MinecraftArguments minecraftArguments = new MinecraftArguments();

        for (String arg : Splitter.on(' ').split(argumentString)) {
            minecraftArguments.getGameArguments().add(new GameArgument(arg));
        }

        setArguments(minecraftArguments);
        setOverridingArguments(true);
    }
}
