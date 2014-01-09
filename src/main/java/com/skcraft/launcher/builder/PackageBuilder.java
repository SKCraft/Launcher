/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.builder;

import com.beust.jcommander.JCommander;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import com.skcraft.launcher.model.modpack.Manifest;
import com.skcraft.launcher.util.SimpleLogFormatter;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;

/**
 * Builds packages for the launcher.
 */
@Log
public class PackageBuilder {

    private final ObjectMapper mapper;
    private ObjectWriter writer;
    private final Manifest manifest;
    @Getter
    private boolean prettyPrint = false;

    /**
     * Create a new package builder.
     *
     * @param mapper the mapper
     * @param manifest the manifest
     */
    public PackageBuilder(@NonNull ObjectMapper mapper, @NonNull Manifest manifest) {
        this.mapper = mapper;
        this.manifest = manifest;
        setPrettyPrint(false); // Set writer
    }

    /**
     * Set whether pretty printing should be used.
     *
     * @param prettyPrint true to pretty print
     */
    public void setPrettyPrint(boolean prettyPrint) {
        if (prettyPrint) {
            writer = mapper.writerWithDefaultPrettyPrinter();
        } else {
            writer = mapper.writer();
        }
        this.prettyPrint = prettyPrint;
    }

    /**
     * Add the files in the given directory.
     *
     * @param dir the directory
     * @param destDir the directory to copy the files to
     * @throws IOException thrown on I/O error
     */
    private void addFiles(File dir, File destDir) throws IOException {
        ClientFileCollector collector = new ClientFileCollector(this.manifest, destDir);
        collector.walk(dir);
    }

    /**
     * Write the manifest to a file.
     *
     * @param path the path
     * @throws IOException thrown on I/O error
     */
    public void writeManifest(@NonNull File path) throws IOException {
        path.getParentFile().mkdirs();
        writer.writeValue(path, manifest);
    }

    /**
     * Parse arguments for the builder.
     *
     * @param args arguments
     * @return options
     */
    private static PackageOptions parseArgs(String[] args) {
        PackageOptions options = new PackageOptions();
        new JCommander(options, args);
        return options;
    }

    /**
     * Build a package given the arguments.
     *
     * @param args arguments
     * @throws IOException thrown on I/O error
     */
    public static void main(String[] args) throws IOException {
        // May throw error here
        PackageOptions options = parseArgs(args);

        SimpleLogFormatter.configureGlobalLogger();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);

        Manifest manifest = new Manifest();
        manifest.setName(options.getName());
        manifest.setTitle(options.getTitle());
        manifest.setVersion(options.getVersion());
        manifest.setGameVersion(options.getGameVersion());
        manifest.setLibrariesLocation(options.getLibrariesLocation());
        manifest.setObjectsLocation(options.getObjectsLocation());

        File path = options.getVersionManifestPath();
        if (path != null) {
            manifest.setVersionManifest(mapper.readValue(path, VersionManifest.class));
        }

        PackageBuilder builder = new PackageBuilder(mapper, manifest);
        builder.setPrettyPrint(options.isPrettyPrinting());

        log.info("Adding files...");
        builder.addFiles(options.getFilesDir(), options.getObjectsDir());
        builder.writeManifest(options.getManifestPath());
        log.info("Wrote manifest to " + options.getManifestPath().getAbsolutePath());
        log.info("Done.");
    }

}
