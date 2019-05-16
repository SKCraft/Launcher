/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.minecraft;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.skcraft.launcher.util.Environment;
import com.skcraft.launcher.util.HttpRequest;

import lombok.Data;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionManifest {

    private String id;
    private Date time;
    private Date releaseTime;
    private String assets;
    private String type;
    private String processArguments;
    // minecraftArguments exists only up to 1.12.2
    private String minecraftArguments;
    // beginning with snapshot 17w43a, it starts getting complicated
    private Arguments arguments;
    private String mainClass;
    private int minimumLauncherVersion;
    // if libraries is a LinkedHashMap, then the Deserializer will "forget" some libraries (they are read from the JSON and added to the internal collection,
    // but are not there when it is assigned to the VersionManifest instance - my best guess is that those are hash collisions)
    // also, the natives member will be empty on all.
    // changing to Library[] fixes both issues, but requires helper functions addLibraries and containsLibarary
    private Library[] libraries;
    private VersionDownloads downloads;
    private VersionAssetIndex assetIndex;

    @JsonIgnore
    private URL fetchURL;

    @JsonIgnore
    public String getAssetsIndex() {
        return getAssets() != null ? getAssets() : "legacy";
    }

    @JsonIgnore
    public void addLibraries(LinkedHashSet<Library> libs) {
        Library[] newLibs = new Library[libraries.length + libs.size()];
        int       idx = 0;

        for (Library lib : libraries) {
            newLibs[idx++] = lib;
        }
        for (Library lib : libs) {
            newLibs[idx++] = lib;
        }
        libraries = newLibs;
    }

    public boolean containsLibrary(Library library) {
        for (Library lib : libraries) {
            if (lib.equals(library))
                return true;
        }
        return false;
    }

    @JsonIgnore
    public URL getClientDownloadURL() {
        return downloads.getClient().getDownloadURL();
    }

    @JsonIgnore
    public static VersionManifest getInstance(URL manifestURL, String version) {
        URL url = ManifestJSON.getVersionURL(manifestURL, version);
        if (url != null) {
            try {
                VersionManifest instance = HttpRequest
                        .get(url)
                        .execute()
                        .expectResponseCode(200)
                        .returnContent()
                        .asJson(VersionManifest.class);
                instance.fetchURL = url;
                return instance;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    @JsonIgnore
    public String[] getVersionIndependentMinecraftArguments() {
        if (minecraftArguments != null) // up to 1.12.2
            return minecraftArguments.split(" +");
        if (arguments != null) // snapshot 17w43a and later
            return arguments.getGameArguments();
        return null;
    }

    @JsonIgnore
    public String[] getJVMArgs() {
        return (arguments != null) ? arguments.getJVMArgs() : null;
    }

    @JsonIgnore
    public List<String> getOSDependentJVMArgs(Environment environment) {
        return (arguments != null) ? arguments.getOSDependentArgs(environment) : null;
    }

}
