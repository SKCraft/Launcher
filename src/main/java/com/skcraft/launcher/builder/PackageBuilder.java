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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

/**
 * Builds packages for the launcher.
 */
@Log
public class PackageBuilder {

    private final ObjectMapper mapper;
    private ObjectWriter writer;
    private final Manifest manifest;
    private final PropertiesApplicator applicator;
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
        this.applicator = new PropertiesApplicator(manifest);
        setPrettyPrint(false); // Set writer
    }

    public void setPrettyPrint(boolean prettyPrint) {
        if (prettyPrint) {
            writer = mapper.writerWithDefaultPrettyPrinter();
        } else {
            writer = mapper.writer();
        }
        this.prettyPrint = prettyPrint;
    }

    public void scan(File dir) throws IOException {
        FileInfoScanner scanner = new FileInfoScanner(mapper);
        scanner.walk(dir);
        for (FeaturePattern pattern : scanner.getPatterns()) {
            applicator.register(pattern);
        }
    }

    public void addFiles(File dir, File destDir) throws IOException {
        ClientFileCollector collector = new ClientFileCollector(this.manifest, applicator, destDir);
        collector.walk(dir);
    }

    public void validateManifest() {
        checkNotNull(emptyToNull(manifest.getName()), "Package name is not defined");
        checkNotNull(emptyToNull(manifest.getGameVersion()), "Game version is not defined");
    }

    public void readConfig(File path) throws IOException {
        if (path != null) {
            BuilderConfig config = read(path, BuilderConfig.class);
            config.update(manifest);
            config.registerProperties(applicator);
        }
    }

    public void readVersionManifest(File path) throws IOException {
        if (path != null) {
            VersionManifest versionManifest = read(path, VersionManifest.class);
            manifest.setVersionManifest(versionManifest);
        }
    }

    public void writeManifest(@NonNull File path) throws IOException {
        manifest.setFeatures(applicator.getFeaturesInUse());
        VersionManifest versionManifest = manifest.getVersionManifest();
        if (versionManifest != null) {
            versionManifest.setId(manifest.getGameVersion());
        }
        validateManifest();
        path.getAbsoluteFile().getParentFile().mkdirs();
        writer.writeValue(path, manifest);
    }

    private static BuilderOptions parseArgs(String[] args) {
        BuilderOptions options = new BuilderOptions();
        new JCommander(options, args);
        return options;
    }

    private <V> V read(File path, Class<V> clazz) throws IOException {
        try {
            if (path == null) {
                return clazz.newInstance();
            } else {
                return mapper.readValue(path, clazz);
            }
        } catch (InstantiationException e) {
            throw new IOException("Failed to create " + clazz.getCanonicalName(), e);
        } catch (IllegalAccessException e) {
            throw new IOException("Failed to create " + clazz.getCanonicalName(), e);
        }
    }

    /**
     * Build a package given the arguments.
     *
     * @param args arguments
     * @throws IOException thrown on I/O error
     */
    public static void main(String[] args) throws IOException {
        BuilderOptions options = parseArgs(args);

        // Initialize
        SimpleLogFormatter.configureGlobalLogger();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);

        Manifest manifest = new Manifest();
        manifest.setMinimumVersion(Manifest.MIN_PROTOCOL_VERSION);
        PackageBuilder builder = new PackageBuilder(mapper, manifest);
        builder.setPrettyPrint(options.isPrettyPrinting());

        // From config
        builder.readConfig(options.getConfigPath());
        builder.readVersionManifest(options.getVersionManifestPath());

        // From options
        manifest.updateName(options.getName());
        manifest.updateTitle(options.getTitle());
        manifest.updateGameVersion(options.getGameVersion());
        manifest.setVersion(options.getVersion());
        manifest.setLibrariesLocation(options.getLibrariesLocation());
        manifest.setObjectsLocation(options.getObjectsLocation());

        builder.scan(options.getFilesDir());
        builder.addFiles(options.getFilesDir(), options.getObjectsDir());
        builder.writeManifest(options.getManifestPath());

        log.info("Wrote manifest to " + options.getManifestPath().getAbsolutePath());
        log.info("Done.");
    }

}
