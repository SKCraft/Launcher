/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.builder;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import lombok.Data;

import java.io.File;

@Data
public class BuilderOptions {

    public static final String DEFAULT_CONFIG_FILENAME = "modpack.json";
    public static final String DEFAULT_VERSION_FILENAME = "version.json";
    public static final String DEFAULT_SRC_DIRNAME = "src";
    public static final String DEFAULT_LOADERS_DIRNAME = "loaders";

    // Configuration

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
    @Parameter(names = "--manifest-dest", required = true)
    private File manifestPath;

    // Overall paths
    @Parameter(names = {"--input", "-i"})
    private File inputPath;
    @Parameter(names = {"--output", "-o"})
    private File outputPath;

    // Input paths
    @Parameter(names = "--config")
    private File configPath;
    @Parameter(names = "--version-file")
    private File versionManifestPath;
    @Parameter(names = "--files")
    private File filesDir;
    @Parameter(names = "--loaders")
    private File loadersDir;

    // Output paths
    @Parameter(names = "--objects-dest")
    private File objectsDir;
    @Parameter(names = "--libraries-dest")
    private File librariesDir;

    @Parameter(names = "--libs-url")
    private String librariesLocation = "libraries";
    @Parameter(names = "--objects-url")
    private String objectsLocation = "objects";

    // Misc
    @Parameter(names = "--pretty-print")
    private boolean prettyPrinting;

    public void choosePaths() throws ParameterException {
        if (configPath == null) {
            requireInputPath("--config");
            configPath = new File(inputPath, DEFAULT_CONFIG_FILENAME);
        }

        if (versionManifestPath == null) {
            requireInputPath("--version");
            versionManifestPath = new File(inputPath, DEFAULT_VERSION_FILENAME);
        }

        if (filesDir == null) {
            requireInputPath("--files");
            filesDir = new File(inputPath, DEFAULT_SRC_DIRNAME);
        }

        if (loadersDir == null) {
            requireInputPath("--loaders");
            loadersDir = new File(inputPath, DEFAULT_LOADERS_DIRNAME);
        }

        if (objectsDir == null) {
            requireOutputPath("--objects-dest");
            objectsDir = new File(outputPath, objectsLocation);
        }

        if (librariesDir == null) {
            requireOutputPath("--libs-dest");
            librariesDir = new File(outputPath, librariesLocation);
        }
    }

    private void requireOutputPath(String name) throws ParameterException {
        if (outputPath == null) {
            throw new ParameterException("Because " + name + " was not specified, --output needs to be specified as the output directory and then " + name + " will be default to a pre-set path within the output directory");
        }
    }

    private void requireInputPath(String name) throws ParameterException {
        if (inputPath == null) {
            throw new ParameterException("Because " + name + " was not specified, --input needs to be specified as the project directory and then " + name + " will be default to a pre-set path within the project directory");
        }
    }

}
