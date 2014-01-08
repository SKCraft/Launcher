/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.File;
import java.net.URL;
import java.util.Date;

@Data
public class Instance implements Comparable<Instance> {

    private String title;
    private String name;
    private String version;
    private boolean updatePending;
    private boolean installed;
    private Date lastAccessed;

    @JsonIgnore private File dir;
    @JsonIgnore private URL manifestURL;
    @JsonIgnore private int priority;
    @JsonIgnore private boolean selected;
    @JsonIgnore private boolean local;

    public String getTitle() {
        return title != null ? title : name;
    }

    @JsonIgnore
    public File getContentDir() {
        return new File(dir, "minecraft");
    }

    @JsonIgnore
    public File getManifestPath() {
        return new File(dir, "manifest.json");
    }

    @JsonIgnore
    public File getVersionManifestPath() {
        return new File(dir, "version.json");
    }

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
