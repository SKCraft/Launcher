/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.loader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.skcraft.launcher.model.minecraft.Library;
import com.skcraft.launcher.model.minecraft.MinecraftArguments;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionInfo {
    private String id;
    @JsonProperty("arguments")
    private MinecraftArguments minecraftArguments;
    private String mainClass;
    private List<Library> libraries;

}
