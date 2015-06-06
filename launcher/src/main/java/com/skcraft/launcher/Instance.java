/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.Files;
import com.skcraft.launcher.launch.JavaProcessBuilder;
import com.skcraft.launcher.model.modpack.LaunchModifier;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

/**
 * An instance is a profile that represents one particular installation
 * of the game, with separate files and so on.
 */
@Data
public class Instance implements Comparable<Instance> {

    private String title;
    private String name;
    private String version;
    private boolean updatePending;
    private boolean installed;
    private Date lastAccessed;
    @JsonProperty("launch")
    private LaunchModifier launchModifier;

    @JsonIgnore private File dir;
    @JsonIgnore private URL manifestURL;
    @JsonIgnore private int priority;
    @JsonIgnore private boolean selected;
    @JsonIgnore private boolean local;

    /**
     * Get the tile of the instance, which might be the same as the
     * instance name if no title is set.
     *
     * @return a title
     */
    public String getTitle() {
        return title != null ? title : name;
    }

    /**
     * Update the given process builder with launch settings that are
     * specific to this instance.
     *
     * @param builder the process builder
     */
    public void modify(JavaProcessBuilder builder) {
        if (launchModifier != null) {
            launchModifier.modify(builder);
        }
    }

    /**
     * Get the instance directory, creating it if possible.
     *
     * @return the directory
     * @see #getContentDir() where game files are stored
     */
    public File getDir() {
        try {
            Files.createParentDirs(dir);
            dir.mkdir();
        } catch (IOException ignored) {
        }
        return dir;
    }

    /**
     * Get the file for the directory where Minecraft's game files are
     * stored, including user files (screenshots, etc.).
     *
     * @return the content directory, which may not exist
     */
    @JsonIgnore
    public File getContentDir() {
        File dir = new File(this.dir, "minecraft");
        try {
            Files.createParentDirs(dir);
            dir.mkdir();
        } catch (IOException ignored) {
        }
        return dir;
    }

    /**
     * Get the file for the package manifest.
     *
     * @return the manifest path, which may not exist
     */
    @JsonIgnore
    public File getManifestPath() {
        return new File(getDir(), "manifest.json");
    }

    /**
     * Get the file for the Minecraft version manfiest file.
     *
     * @return the version path, which may not exist
     */
    @JsonIgnore
    public File getVersionPath() {
        return new File(getDir(), "version.json");
    }

    /**
     * Get the file for the custom JAR file.
     *
     * @return the JAR file, which may not exist
     */
    @JsonIgnore
    public File getCustomJarPath() {
        return new File(getContentDir(), "custom_jar.jar");
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public int compareTo(Instance o) {
        if (isLocal() && !o.isLocal()) {
            return -1;
        } else if (!isLocal() && o.isLocal()) {
            return 1;
        } else if (isLocal() && o.isLocal()) {
            Date otherDate = o.getLastAccessed();

            if (otherDate == null && lastAccessed == null) {
                return 0;
            } else if (otherDate == null) {
                return -1;
            } else if (lastAccessed == null) {
                return 1;
            } else {
                return -lastAccessed.compareTo(otherDate);
            }
        } else {
            if (priority > o.priority) {
                return -1;
            } else if (priority < o.priority) {
                return 1;
            } else {
                return 0;
            }
        }
    }

}
