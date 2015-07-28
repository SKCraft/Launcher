/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.model.creator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.skcraft.launcher.builder.BuilderConfig;
import com.skcraft.launcher.builder.BuilderOptions;
import com.skcraft.launcher.persistence.Persistence;
import lombok.Data;

import java.io.File;

@Data
public class Pack {

    private String location;
    @JsonIgnore private Workspace workspace;
    @JsonIgnore private BuilderConfig cachedConfig;

    @JsonIgnore
    public File getDirectory() {
        File path = new File(location);
        if (path.isAbsolute()) {
            return path;
        } else {
            return new File(workspace.getDirectory(), location);
        }
    }

    @JsonIgnore
    public File getLoadersDir() {
        return new File(getDirectory(), "loaders");
    }

    @JsonIgnore
    public File getSourceDir() {
        return new File(getDirectory(), "src");
    }

    @JsonIgnore
    public File getModsDir() {
        return new File(getSourceDir(), "mods");
    }

    @JsonIgnore
    public File getConfigFile() {
        return new File(getDirectory(), BuilderOptions.DEFAULT_CONFIG_FILENAME);
    }

    public void load() {
        setCachedConfig(Persistence.read(getConfigFile(), BuilderConfig.class, true));
        getLoadersDir().mkdirs();
        getSourceDir().mkdirs();
    }

    public void createGuideFolders() {
        new File(getSourceDir(), "config").mkdirs();
        new File(getSourceDir(), "mods").mkdirs();
        new File(getSourceDir(), "resourcepacks").mkdirs();
    }

    @JsonIgnore
    public boolean isLoaded() {
        return cachedConfig != null;
    }

}
