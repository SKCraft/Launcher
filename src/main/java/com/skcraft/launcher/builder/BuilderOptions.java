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
public class BuilderOptions {

    // Configuration
    @Parameter(names = "--config")
    private File configPath;
    @Parameter(names = "--version-file")
    private File versionManifestPath;
    @Parameter(names = "--libs-url")
    private String librariesLocation;
    @Parameter(names = "--objects-url")
    private String objectsLocation;

    // Override config
    @Parameter(names = "--name")
    private String name;
    @Parameter(names = "--title")
    private String title;
    @Parameter(names = "--mc-version")
    private String gameVersion;

    // Required
    @Parameter(names = "--version", required = true)
    private String version;

    // Paths
    @Parameter(names = "--files", required = true)
    private File filesDir;
    @Parameter(names = "--manifest-dest", required = true)
    private File manifestPath;
    @Parameter(names = "--objects-dest", required = true)
    private File objectsDir;

    // Misc
    @Parameter(names = "--pretty-print")
    private boolean prettyPrinting;

}
