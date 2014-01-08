/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.builder;

import com.beust.jcommander.Parameter;
import lombok.Data;

import java.io.File;

@Data
public class PackageOptions {

    @Parameter(names = "--name", required = true)
    private String name;

    @Parameter(names = "--title", required = true)
    private String title;

    @Parameter(names = "--version", required = true)
    private String version;

    @Parameter(names = "--mc-version", required = true)
    private String gameVersion;

    @Parameter(names = "--manifest-path", required = true)
    private File manifestPath;

    @Parameter(names = "--objects-dest", required = true)
    private File objectsDir;

    @Parameter(names = "--files", required = true)
    private File filesDir;

    @Parameter(names = "--version-file")
    private File versionManifestPath;

    @Parameter(names = "--libs-url")
    private String librariesLocation;

    @Parameter(names = "--objects-url")
    private String objectsLocation;

}
