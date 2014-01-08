/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.modpack;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.google.common.base.Strings;
import com.skcraft.launcher.LauncherUtils;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import com.skcraft.launcher.update.Installer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Manifest extends BaseManifest {

    private URL baseUrl;
    private String librariesLocation;
    private String objectsLocation;
    private String gameVersion;
    @JsonManagedReference("manifest")
    private List<Task> tasks = new ArrayList<Task>();
    @Getter @Setter @JsonIgnore
    private Installer installer;
    private VersionManifest versionManifest;

    @JsonIgnore
    public URL getLibrariesURL() {
        if (Strings.nullToEmpty(getLibrariesLocation()) == null) {
            return baseUrl;
        }

        try {
            return LauncherUtils.concat(baseUrl, Strings.nullToEmpty(getLibrariesLocation()) + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonIgnore
    public URL getObjectsURL() {
        if (Strings.nullToEmpty(getObjectsLocation()) == null) {
            return baseUrl;
        }

        try {
            return LauncherUtils.concat(baseUrl, Strings.nullToEmpty(getObjectsLocation()) + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
