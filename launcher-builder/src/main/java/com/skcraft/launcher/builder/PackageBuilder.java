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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import com.google.common.io.Files;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.LauncherUtils;
import com.skcraft.launcher.builder.loaders.*;
import com.skcraft.launcher.model.loader.BasicInstallProfile;
import com.skcraft.launcher.model.minecraft.Library;
import com.skcraft.launcher.model.minecraft.ReleaseList;
import com.skcraft.launcher.model.minecraft.Version;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import com.skcraft.launcher.model.modpack.Manifest;
import com.skcraft.launcher.util.HttpRequest;
import com.skcraft.launcher.util.SimpleLogFormatter;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.java.Log;

import java.io.*;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.zip.ZipEntry;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;
import static com.skcraft.launcher.util.HttpRequest.url;

/**
 * Builds packages for the launcher.
 */
@Log
public class PackageBuilder {
    private final Properties properties;
    private final ObjectMapper mapper;
    private ObjectWriter writer;
    private final Manifest manifest;
    private final PropertiesApplicator applicator;
    @Getter
    private boolean prettyPrint = false;

    @Getter @Setter
    private File baseDir;

    private List<Library> loaderLibraries = Lists.newArrayList();
    private List<Library> installerLibraries = Lists.newArrayList();
    private List<String> mavenRepos;
    private List<URL> jarMavens = Lists.newArrayList();

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

        FileInfoScanner infoScanner = new FileInfoScanner(mapper);
        infoScanner.walk(dir);
        for (FeaturePattern pattern : infoScanner.getPatterns()) {
            applicator.register(pattern);
        }

        logSection("Scanning for .url.txt files...");
        FileUrlScanner urlScanner = new FileUrlScanner();
        urlScanner.walk(dir);
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
        ILoaderProcessor processor = null;

        try {
            ZipEntry profileEntry = BuilderUtils.getZipEntry(jarFile, "install_profile.json");

            if (profileEntry != null) {
                InputStream stream = jarFile.getInputStream(profileEntry);
                InputStreamReader reader = closer.register(new InputStreamReader(stream));

                BasicInstallProfile basicProfile = mapper.readValue(BuilderUtils.readStringFromStream(reader),
                        BasicInstallProfile.class);

                if (basicProfile.isLegacy()) {
                    processor = new OldForgeLoaderProcessor();
                } else {
                    processor = new ModernForgeLoaderProcessor();
                }
            } else if (BuilderUtils.getZipEntry(jarFile, "fabric-installer.json") != null) {
            	processor = new FabricLoaderProcessor(FabricLoaderProcessor.Variant.FABRIC);
            } else if (BuilderUtils.getZipEntry(jarFile, "quilt_installer.json") != null) {
                processor = new FabricLoaderProcessor(FabricLoaderProcessor.Variant.QUILT);
            }
        } finally {
            closer.close();
            jarFile.close();
        }

        if (processor != null) {
            LoaderResult result = processor.process(file, manifest, mapper, baseDir);

            if (result == null) {
                log.warning("Loader " + file.getName() + " failed to process.");
                return;
            }

            loaderLibraries.addAll(result.getLoaderLibraries());
            installerLibraries.addAll(result.getProcessorLibraries());
            jarMavens.addAll(result.getJarMavens());
        } else {
            log.warning("Loader " + file.getName() + " was skipped due to missing metadata. " +
                    "Is it really a loader JAR?");
        }
    }

    public void downloadLibraries(File librariesDir) throws IOException, InterruptedException {
        logSection("Downloading libraries...");

        for (Library library : Iterables.concat(loaderLibraries, installerLibraries)) {
            library.ensureDownloadsExist();

            for (Library.Artifact artifact : library.getDownloads().getAllArtifacts()) {
                File outputPath = new File(librariesDir, artifact.getPath());

                if (!outputPath.exists()) {
                    Files.createParentDirs(outputPath);
                    boolean found = false;
                    boolean urlEmpty = artifact.getUrl().isEmpty();

                    // If URL doesn't end with a /, it might be the direct file
                    if (!urlEmpty && !artifact.getUrl().endsWith("/")) {
                        found = tryDownloadLibrary(library, artifact, artifact.getUrl(), outputPath);
                    }

                    // Look inside the loader JARs
                    if (!found) {
                        for (URL base : jarMavens) {
                            found = tryFetchLibrary(library, new URL(base, artifact.getPath()), outputPath);
                            if (found) break;
                        }
                    }

                    // Assume artifact URL is a maven repository URL and try that
                    if (!found && !urlEmpty) {
                        URL url = LauncherUtils.concat(url(artifact.getUrl()), artifact.getPath());
                        found = tryDownloadLibrary(library, artifact, url.toString(), outputPath);
                    }

                    // Try each repository if not found yet
                    if (!found) {
                        for (String baseUrl : mavenRepos) {
                            found = tryDownloadLibrary(library, artifact, baseUrl + artifact.getPath(),
                                    outputPath);
                            if (found) break;
                        }
                    }

                    if (!found) {
                        log.warning("!! Failed to download the library " + library.getName() +
                                " -- this means your copy of the libraries will lack this file");
                    }
                }
            }
        }
    }

    private boolean tryDownloadLibrary(Library library, Library.Artifact artifact, String baseUrl, File outputPath)
            throws IOException, InterruptedException {
        URL url = new URL(baseUrl);

        if (url.getPath().isEmpty() || url.getPath().equals("/")) {
            // empty path, this is probably the first "is this a full URL" try.
            return false;
        }

        // Some repositories compress their files
        List<Compressor> compressors = BuilderUtils.getCompressors(baseUrl);
        for (Compressor compressor : Lists.reverse(compressors)) {
            url = new URL(compressor.transformPathname(url.toString()));
        }

        File tempFile = File.createTempFile("launcherlib", null);

        try {
            log.info("Downloading library " + library.getName() + " from " + url + "...");
            HttpRequest.get(url).execute().expectResponseCode(200)
                    .expectContentType("application/java-archive", "application/octet-stream", "application/zip")
                    .saveContent(tempFile);
        } catch (IOException e) {
            log.info("Could not get file from " + url + ": " + e.getMessage());
            return false;
        }

        writeLibraryToFile(outputPath, tempFile, compressors);
        return true;
    }

    private boolean tryFetchLibrary(Library library, URL url, File outputPath)
            throws IOException {
        File tempFile = File.createTempFile("launcherlib", null);

        Closer closer = Closer.create();
        try {
            log.info("Reading library " + library.getName() + " from " + url.toString());
            InputStream stream = closer.register(url.openStream());
            stream = closer.register(new BufferedInputStream(stream));

            ByteStreams.copy(stream, closer.register(new FileOutputStream(tempFile)));
        } catch (IOException e) {
            log.info("Could not get file from " + url + ": " + e.getMessage());
            return false;
        } finally {
            closer.close();
        }

        writeLibraryToFile(outputPath, tempFile, Collections.<Compressor>emptyList());
        return true;
    }

    private void writeLibraryToFile(File outputPath, File inputFile, List<Compressor> compressors) throws IOException {
        // Decompress (if needed) and write to file
        Closer closer = Closer.create();
        InputStream inputStream = closer.register(new FileInputStream(inputFile));
        inputStream = closer.register(new BufferedInputStream(inputStream));
        for (Compressor compressor : compressors) {
            inputStream = closer.register(compressor.createInputStream(inputStream));
        }
        ByteStreams.copy(inputStream, closer.register(new FileOutputStream(outputPath)));

        inputFile.delete();
        closer.close();
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
            URL url = url(properties.getProperty("versionManifestUrl"));

            log.info("Fetching version manifest from " + url + "...");

            ReleaseList releases = HttpRequest.get(url)
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .asJson(ReleaseList.class);

            Version version = releases.find(manifest.getGameVersion());
            VersionManifest versionManifest = HttpRequest.get(url(version.getUrl()))
                .execute()
                .expectResponseCode(200)
                .returnContent()
                .asJson(VersionManifest.class);

            manifest.setVersionManifest(versionManifest);
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
        new JCommander(options).parse(args);
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
        mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

        Manifest manifest = new Manifest();
        manifest.setMinimumVersion(Manifest.MIN_PROTOCOL_VERSION);
        PackageBuilder builder = new PackageBuilder(mapper, manifest);
        builder.setPrettyPrint(options.isPrettyPrinting());

        // From config
        builder.readConfig(options.getConfigPath());
        builder.readVersionManifest(options.getVersionManifestPath());
        builder.setBaseDir(options.getOutputPath());

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
