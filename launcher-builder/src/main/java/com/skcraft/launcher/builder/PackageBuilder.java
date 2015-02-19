/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.builder;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Closer;
import com.google.common.io.Files;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.LauncherUtils;
import com.skcraft.launcher.model.loader.InstallProfile;
import com.skcraft.launcher.model.minecraft.Library;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import com.skcraft.launcher.model.modpack.Manifest;
import com.skcraft.launcher.util.Environment;
import com.skcraft.launcher.util.HttpRequest;
import com.skcraft.launcher.util.SimpleLogFormatter;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.*;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;
import static com.skcraft.launcher.util.HttpRequest.url;

/**
 * Builds packages for the launcher.
 */
@Log
public class PackageBuilder {

    private static final Pattern TWEAK_CLASS_ARG = Pattern.compile("--tweakClass\\s+([^\\s]+)");

    private final Properties properties;
    private final ObjectMapper mapper;
    private ObjectWriter writer;
    private final Manifest manifest;
    private final PropertiesApplicator applicator;
    @Getter
    private boolean prettyPrint = false;
    private List<Library> loaderLibraries = Lists.newArrayList();
    private List<String> mavenRepos;

    /**
     * Create a new package builder.
     *
     * @param mapper the mapper
     * @param manifest the manifest
     */
    public PackageBuilder(@NonNull ObjectMapper mapper, @NonNull Manifest manifest) throws IOException {
        this.properties = LauncherUtils.loadProperties(Launcher.class,
                "launcher.properties", "com.skcraft.launcher.propertiesFile");

        this.mapper = mapper;
        this.manifest = manifest;
        this.applicator = new PropertiesApplicator(manifest);
        setPrettyPrint(false); // Set writer

        Closer closer = Closer.create();
        try {
            mavenRepos = mapper.readValue(closer.register(Launcher.class.getResourceAsStream("maven_repos.json")), new TypeReference<List<String>>() {
            });
        } finally {
            closer.close();
        }
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
        logSection("Scanning for .info.json files...");

        FileInfoScanner scanner = new FileInfoScanner(mapper);
        scanner.walk(dir);
        for (FeaturePattern pattern : scanner.getPatterns()) {
            applicator.register(pattern);
        }
    }

    public void addFiles(File dir, File destDir) throws IOException {
        logSection("Adding files to modpack...");

        ClientFileCollector collector = new ClientFileCollector(this.manifest, applicator, destDir);
        collector.walk(dir);
    }

    public void addLoaders(File dir, File librariesDir) {
        logSection("Checking for mod loaders to install...");

        LinkedHashSet<Library> collected = new LinkedHashSet<Library>();

        File[] files = dir.listFiles(new JarFileFilter());
        if (files != null) {
            for (File file : files) {
                try {
                    processLoader(collected, file, librariesDir);
                } catch (IOException e) {
                    log.log(Level.WARNING, "Failed to add the loader at " + file.getAbsolutePath(), e);
                }
            }
        }

        this.loaderLibraries.addAll(collected);

        VersionManifest version = manifest.getVersionManifest();
        collected.addAll(version.getLibraries());
        version.setLibraries(collected);
    }

    private void processLoader(LinkedHashSet<Library> loaderLibraries, File file, File librariesDir) throws IOException {
        log.info("Installing " + file.getName() + "...");

        JarFile jarFile = new JarFile(file);
        Closer closer = Closer.create();

        try {
            ZipEntry profileEntry = BuilderUtils.getZipEntry(jarFile, "install_profile.json");

            if (profileEntry != null) {
                InputStream stream = jarFile.getInputStream(profileEntry);

                // Read file
                String data = CharStreams.toString(closer.register(new InputStreamReader(stream)));
                data = data.replaceAll(",\\s*\\}", "}"); // Fix issues with trailing commas

                InstallProfile profile = mapper.readValue(data, InstallProfile.class);
                VersionManifest version = manifest.getVersionManifest();

                // Copy tweak class arguments
                String args = profile.getVersionInfo().getMinecraftArguments();
                if (args != null) {
                    String existingArgs = Strings.nullToEmpty(version.getMinecraftArguments());

                    Matcher m = TWEAK_CLASS_ARG.matcher(args);
                    while (m.find()) {
                        version.setMinecraftArguments(existingArgs + " " + m.group());
                        log.info("Adding " + m.group() + " to launch arguments");
                    }
                }

                // Add libraries
                List<Library> libraries = profile.getVersionInfo().getLibraries();
                if (libraries != null) {
                    for (Library library : libraries) {
                        if (!version.getLibraries().contains(library)) {
                            loaderLibraries.add(library);
                        }
                    }
                }

                // Copy main class
                String mainClass = profile.getVersionInfo().getMainClass();
                if (mainClass != null) {
                    version.setMainClass(mainClass);
                    log.info("Using " + mainClass + " as the main class");
                }

                // Extract the library
                String filePath = profile.getInstallData().getFilePath();
                String libraryPath = profile.getInstallData().getPath();

                if (filePath != null && libraryPath != null) {
                    ZipEntry libraryEntry = BuilderUtils.getZipEntry(jarFile, filePath);

                    if (libraryEntry != null) {
                        Library library = new Library();
                        library.setName(libraryPath);
                        File extractPath = new File(librariesDir, library.getPath(Environment.getInstance()));
                        Files.createParentDirs(extractPath);
                        ByteStreams.copy(closer.register(jarFile.getInputStream(libraryEntry)), Files.newOutputStreamSupplier(extractPath));
                    } else {
                        log.warning("Could not find the file '" + filePath + "' in " + file.getAbsolutePath() + ", which means that this mod loader will not work correctly");
                    }
                }
            } else {
                log.warning("The file at " + file.getAbsolutePath() + " did not appear to have an " +
                        "install_profile.json file inside -- is it actually an installer for a mod loader?");
            }
        } finally {
            closer.close();
            jarFile.close();
        }
    }

    public void downloadLibraries(File librariesDir) throws IOException, InterruptedException {
        logSection("Downloading libraries...");

        // TODO: Download libraries for different environments -- As of writing, this is not an issue
        Environment env = Environment.getInstance();

        for (Library library : loaderLibraries) {
            File outputPath = new File(librariesDir, library.getPath(env));

            if (!outputPath.exists()) {
                Files.createParentDirs(outputPath);
                boolean found = false;

                // Gather a list of repositories to download from
                List<String> sources = Lists.newArrayList();
                if (library.getBaseUrl() != null) {
                    sources.add(library.getBaseUrl());
                }
                sources.addAll(mavenRepos);

                // Try each repository
                for (String baseUrl : sources) {
                    String pathname = library.getPath(env);

                    // Some repositories compress their files
                    List<Compressor> compressors = BuilderUtils.getCompressors(baseUrl);
                    for (Compressor compressor : Lists.reverse(compressors)) {
                        pathname = compressor.transformPathname(pathname);
                    }

                    URL url = new URL(baseUrl + pathname);
                    File tempFile = File.createTempFile("launcherlib", null);

                    try {
                        log.info("Downloading library " + library.getName() + " from " + url + "...");
                        HttpRequest.get(url).execute().expectResponseCode(200).saveContent(tempFile);
                    } catch (IOException e) {
                        log.info("Could not get file from " + url + ": " + e.getMessage());
                        continue;
                    }

                    // Decompress (if needed) and write to file
                    Closer closer = Closer.create();
                    InputStream inputStream = closer.register(new FileInputStream(tempFile));
                    inputStream = closer.register(new BufferedInputStream(inputStream));
                    for (Compressor compressor : compressors) {
                        inputStream = closer.register(compressor.createInputStream(inputStream));
                    }
                    ByteStreams.copy(inputStream, closer.register(new FileOutputStream(outputPath)));

                    tempFile.delete();

                    found = true;
                    break;
                }

                if (!found) {
                    log.warning("!! Failed to download the library " + library.getName() + " -- this means your copy of the libraries will lack this file");
                }
            }
        }
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

    public void readVersionManifest(File path) throws IOException, InterruptedException {
        logSection("Reading version manifest...");

        if (path.exists()) {
            VersionManifest versionManifest = read(path, VersionManifest.class);
            manifest.setVersionManifest(versionManifest);

            log.info("Loaded version manifest from " + path.getAbsolutePath());
        } else {
            URL url = url(String.format(
                    properties.getProperty("versionManifestUrl"),
                    manifest.getGameVersion()));

            log.info("Fetching version manifest from " + url + "...");

            manifest.setVersionManifest(HttpRequest
                    .get(url)
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .asJson(VersionManifest.class));
        }
    }

    public void writeManifest(@NonNull File path) throws IOException {
        logSection("Writing manifest...");

        manifest.setFeatures(applicator.getFeaturesInUse());
        VersionManifest versionManifest = manifest.getVersionManifest();
        if (versionManifest != null) {
            versionManifest.setId(manifest.getGameVersion());
        }
        validateManifest();
        path.getAbsoluteFile().getParentFile().mkdirs();
        writer.writeValue(path, manifest);

        log.info("Wrote manifest to " + path.getAbsolutePath());
    }

    private static BuilderOptions parseArgs(String[] args) {
        BuilderOptions options = new BuilderOptions();
        new JCommander(options, args);
        options.choosePaths();
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
     * @throws InterruptedException on interruption
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        BuilderOptions options;
        try {
            options = parseArgs(args);
        } catch (ParameterException e) {
            new JCommander().usage();
            System.err.println("error: " + e.getMessage());
            System.exit(1);
            return;
        }

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
        builder.addLoaders(options.getLoadersDir(), options.getLibrariesDir());
        builder.downloadLibraries(options.getLibrariesDir());
        builder.writeManifest(options.getManifestPath());

        logSection("Done");

        log.info("Now upload the contents of " + options.getOutputPath() + " to your web server or CDN!");
    }

    private static void logSection(String name) {
        log.info("");
        log.info("--- " + name + " ---");
    }

}
