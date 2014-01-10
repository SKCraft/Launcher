/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.modpack;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.LauncherUtils;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import com.skcraft.launcher.install.Installer;
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

    public static final int MIN_PROTOCOL_VERSION = 2;

    private int minimumVersion;
    private URL baseUrl;
    private String librariesLocation;
    private String objectsLocation;
    private String gameVersion;
    @JsonProperty("launch")
    private LaunchModifier launchModifier;
    private List<Feature> features = new ArrayList<Feature>();
    @JsonManagedReference("manifest")
    private List<ManifestEntry> tasks = new ArrayList<ManifestEntry>();
    @Getter @Setter @JsonIgnore
    private Installer installer;
    private VersionManifest versionManifest;

    @JsonIgnore
    public URL getLibrariesUrl() {
        if (Strings.nullToEmpty(getLibrariesLocation()) == null) {
            return null;
        }

        try {
            return LauncherUtils.concat(baseUrl, Strings.nullToEmpty(getLibrariesLocation()) + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonIgnore
    public URL getObjectsUrl() {
        if (Strings.nullToEmpty(getObjectsLocation()) == null) {
            return baseUrl;
        }

        try {
            return LauncherUtils.concat(baseUrl, Strings.nullToEmpty(getObjectsLocation()) + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateName(String name) {
        if (name != null) {
            setName(name);
        }
    }

    public void updateTitle(String title) {
        if (title != null) {
            setTitle(title);
        }
    }

    public void updateGameVersion(String gameVersion) {
        if (gameVersion != null) {
            setGameVersion(gameVersion);
        }
    }

    public void update(Instance instance) {
        instance.setLaunchModifier(getLaunchModifier());
    }
}
