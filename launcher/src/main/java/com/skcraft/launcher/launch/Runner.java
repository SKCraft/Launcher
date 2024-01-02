/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.launch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.skcraft.concurrency.DefaultProgress;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.*;
import com.skcraft.launcher.auth.Session;
import com.skcraft.launcher.install.ZipExtract;
import com.skcraft.launcher.launch.runtime.JavaRuntime;
import com.skcraft.launcher.launch.runtime.JavaRuntimeFinder;
import com.skcraft.launcher.model.minecraft.*;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.util.Environment;
import com.skcraft.launcher.util.Platform;
import com.skcraft.launcher.util.SharedLocale;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.java.Log;
import org.apache.commons.lang.text.StrSubstitutor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.function.BiPredicate;

import static com.skcraft.launcher.LauncherUtils.checkInterrupted;
import static com.skcraft.launcher.util.SharedLocale.tr;

/**
 * Handles the launching of an instance.
 */
@Log
public class Runner implements Callable<Process>, ProgressObservable {

    private ProgressObservable progress = new DefaultProgress(0, SharedLocale.tr("runner.preparing"));

    private final ObjectMapper mapper = new ObjectMapper();
    private final Launcher launcher;
    private final Instance instance;
    private final Session session;
    private final File extractDir;
    private final BiPredicate<JavaRuntime, JavaVersion> javaRuntimeMismatch;
    @Getter @Setter private Environment environment = Environment.getInstance();

    private VersionManifest versionManifest;
    private AssetsIndex assetsIndex;
    private File virtualAssetsDir;
    private Configuration config;
    private JavaProcessBuilder builder;
    private AssetsRoot assetsRoot;
    private FeatureList.Mutable featureList;

    /**
     * Create a new instance launcher.
     *  @param launcher the launcher
     * @param instance the instance
     * @param session the session
     * @param extractDir the directory to extract to
     * @param javaRuntimeMismatch
     */
    public Runner(@NonNull Launcher launcher, @NonNull Instance instance,
                  @NonNull Session session, @NonNull File extractDir,
                  BiPredicate<JavaRuntime, JavaVersion> javaRuntimeMismatch) {
        this.launcher = launcher;
        this.instance = instance;
        this.session = session;
        this.extractDir = extractDir;
        this.javaRuntimeMismatch = javaRuntimeMismatch;
        this.featureList = new FeatureList.Mutable();
    }

    /**
     * Get the path to the JAR.
     *
     * @return the JAR path
     */
    private File getJarPath() {
        File jarPath = instance.getCustomJarPath();
        if (!jarPath.exists()) {
            jarPath = launcher.getJarPath(versionManifest);
        }
        return jarPath;
    }

    @Override
    public Process call() throws Exception {
        if (!instance.isInstalled()) {
            throw new LauncherException("Update required", SharedLocale.tr("runner.updateRequired"));
        }

        config = launcher.getConfig();
        builder = new JavaProcessBuilder();
        assetsRoot = launcher.getAssets();

        // Load manifiests
        versionManifest = mapper.readValue(instance.getVersionPath(), VersionManifest.class);

        // Load assets index
        File assetsFile = assetsRoot.getIndexPath(versionManifest);
        try {
            assetsIndex = mapper.readValue(assetsFile, AssetsIndex.class);
        } catch (FileNotFoundException e) {
            instance.setInstalled(false);
            Persistence.commitAndForget(instance);
            throw new LauncherException("Missing assets index " + assetsFile.getAbsolutePath(),
                    tr("runner.missingAssetsIndex", instance.getTitle(), assetsFile.getAbsolutePath()));
        } catch (IOException e) {
            instance.setInstalled(false);
            Persistence.commitAndForget(instance);
            throw new LauncherException("Corrupt assets index " + assetsFile.getAbsolutePath(),
                    tr("runner.corruptAssetsIndex", instance.getTitle(), assetsFile.getAbsolutePath()));
        }

        // Copy over assets to the tree
        try {
            AssetsRoot.AssetsTreeBuilder assetsBuilder = assetsRoot.createAssetsBuilder(versionManifest);
            progress = assetsBuilder;
            virtualAssetsDir = assetsBuilder.build();
        } catch (LauncherException e) {
            instance.setInstalled(false);
            Persistence.commitAndForget(instance);
            throw e;
        }

        progress = new DefaultProgress(0.9, SharedLocale.tr("runner.collectingArgs"));
        builder.setMainClass(versionManifest.getMainClass());

        addWindowArgs();
        addLibraries();
        addJvmArgs();
        addJarArgs();
        addProxyArgs();
        addServerArgs();
        addPlatformArgs();
        addLegacyArgs();

        callLaunchModifier();

        verifyJavaRuntime();

        ProcessBuilder processBuilder = new ProcessBuilder(builder.buildCommand());
        processBuilder.directory(instance.getContentDir());
        Runner.log.info("Launching: " + builder);
        checkInterrupted();

        progress = new DefaultProgress(1, SharedLocale.tr("runner.startingJava"));

        return processBuilder.start();
    }

    /**
     * Call the manifest launch modifier.
     */
    private void callLaunchModifier() {
        instance.modify(builder);
    }

    private void verifyJavaRuntime() {
        JavaRuntime pickedRuntime = builder.getRuntime();
        JavaVersion targetVersion = versionManifest.getJavaVersion();

        if (pickedRuntime == null || targetVersion == null) {
            return;
        }

        if (pickedRuntime.getMajorVersion() != targetVersion.getMajorVersion()) {
            boolean launchAnyway = javaRuntimeMismatch.test(pickedRuntime, targetVersion);

            if (!launchAnyway) {
                throw new CancellationException("Launch cancelled by user.");
            }
        }
    }

    /**
     * Add platform-specific arguments.
     */
    private void addPlatformArgs() {
        // Mac OS X arguments
        if (getEnvironment().getPlatform() == Platform.MAC_OS_X) {
            File icnsPath = assetsIndex.getObjectPath(assetsRoot, "icons/minecraft.icns");
            if (icnsPath != null) {
                builder.getFlags().add("-Xdock:icon=" + icnsPath.getAbsolutePath());
                builder.getFlags().add("-Xdock:name=Minecraft");
            }
        }
    }

    /**
     * Add libraries.
     */
    private void addLibraries() throws LauncherException {
        // Add libraries to classpath or extract the libraries as necessary
        for (Library library : versionManifest.getLibraries()) {
            if (!library.matches(environment)) {
                continue;
            }

            File path = new File(launcher.getLibrariesDir(), library.getPath(environment));

            if (path.exists()) {
                Library.Extract extract = library.getExtract();
                if (extract != null) {
                    ZipExtract zipExtract = new ZipExtract(Files.asByteSource(path), extractDir);
                    zipExtract.setExclude(extract.getExclude());
                    zipExtract.run();
                } else {
                    builder.classPath(path);
                }
            } else {
                instance.setInstalled(false);
                Persistence.commitAndForget(instance);
                throw new LauncherException("Missing library " + library.getName(),
                        tr("runner.missingLibrary", instance.getTitle(), library.getName()));
            }
        }

        // The official launcher puts the vanilla jar at the end of the classpath, we'll do the same
        builder.classPath(getJarPath());
    }

    /**
     * Add JVM arguments.
     *
     * @throws IOException on I/O error
     */
    private void addJvmArgs() throws IOException, LauncherException {
        Optional<MemorySettings> memorySettings = Optional.ofNullable(instance.getSettings().getMemorySettings());

        int minMemory = memorySettings
                .map(MemorySettings::getMinMemory)
                .orElse(config.getMinMemory());

        int maxMemory = memorySettings
                .map(MemorySettings::getMaxMemory)
                .orElse(config.getMaxMemory());

        int permGen = config.getPermGen();

        if (minMemory <= 0) {
            minMemory = 1024;
        }

        if (maxMemory <= 0) {
            maxMemory = 1024;
        }

        if (permGen <= 0) {
            permGen = 128;
        }

        if (permGen <= 64) {
            permGen = 64;
        }

        if (minMemory > maxMemory) {
            maxMemory = minMemory;
        }

        builder.setMinMemory(minMemory);
        builder.setMaxMemory(maxMemory);
        builder.setPermGen(permGen);

        JavaRuntime selectedRuntime = Optional.ofNullable(instance.getSettings().getRuntime())
                .orElseGet(() -> Optional.ofNullable(versionManifest.getJavaVersion())
                        .flatMap(JavaRuntimeFinder::findBestJavaRuntime)
                        .orElse(config.getJavaRuntime())
                );

        // Builder defaults to the PATH `java` if the runtime is null
        builder.setRuntime(selectedRuntime);

        List<String> flags = builder.getFlags();
        String[] rawJvmArgsList = new String[] {
                config.getJvmArgs(),
                instance.getSettings().getCustomJvmArgs()
        };

        for (String rawJvmArgs : rawJvmArgsList) {
            if (!Strings.isNullOrEmpty(rawJvmArgs)) {
                flags.addAll(JavaProcessBuilder.splitArgs(rawJvmArgs));
            }
        }

        List<GameArgument> javaArguments = versionManifest.getArguments().getJvmArguments();
        StrSubstitutor substitutor = new StrSubstitutor(getCommandSubstitutions());
        for (GameArgument arg : javaArguments) {
            if (arg.shouldApply(environment, featureList)) {
                for (String subArg : arg.getValues()) {
                    flags.add(substitutor.replace(subArg));
                }
            }
        }

        if (versionManifest.getLogging() != null && versionManifest.getLogging().getClient() != null) {
            log.info("Logging config present, log4j2 bug likely mitigated");

            VersionManifest.LoggingConfig config = versionManifest.getLogging().getClient();
            File configFile = new File(launcher.getLibrariesDir(), config.getFile().getId());
            StrSubstitutor loggingSub = new StrSubstitutor(ImmutableMap.of("path", configFile.getAbsolutePath()));

            flags.add(loggingSub.replace(config.getArgument()));
        }
    }

    /**
     * Add arguments for the application.
     *
     * @throws JsonProcessingException on error
     */
    private void addJarArgs() throws JsonProcessingException {
        List<String> args = builder.getArgs();

        List<GameArgument> rawArgs = versionManifest.getArguments().getGameArguments();
        StrSubstitutor substitutor = new StrSubstitutor(getCommandSubstitutions());
        for (GameArgument arg : rawArgs) {
            if (arg.shouldApply(environment, featureList)) {
                for (String subArg : arg.getValues()) {
                    args.add(substitutor.replace(subArg));
                }
            }
        }
    }

    /**
     * Add proxy arguments.
     */
    private void addProxyArgs() {
        List<String> args = builder.getArgs();

        if (config.isProxyEnabled()) {
            String host = config.getProxyHost();
            int port = config.getProxyPort();
            String username = config.getProxyUsername();
            String password = config.getProxyPassword();

            if (!Strings.isNullOrEmpty(host) && port > 0 && port < 65535) {
                args.add("--proxyHost");
                args.add(config.getProxyHost());
                args.add("--proxyPort");
                args.add(String.valueOf(port));

                if (!Strings.isNullOrEmpty(username)) {
                    builder.getArgs().add("--proxyUser");
                    builder.getArgs().add(username);
                    builder.getArgs().add("--proxyPass");
                    builder.getArgs().add(password);
                }
            }
        }
    }

    /**
     * Add server arguments.
     */
    private void addServerArgs() {
        List<String> args = builder.getArgs();

        if (config.isServerEnabled()) {
            String host = config.getServerHost();
            int port = config.getServerPort();

            if (!Strings.isNullOrEmpty(host) && port > 0 && port < 65535) {
                args.add("--server");
                args.add(host);
                args.add("--port");
                args.add(String.valueOf(port));
            }
        }
    }

    /**
     * Add window arguments.
     */
    private void addWindowArgs() {
        int width = config.getWindowWidth();

        if (width >= 10) {
            featureList.addFeature("has_custom_resolution", true);
        }
    }

    /**
     * Add arguments to make legacy Minecraft work.
     */
    private void addLegacyArgs() {
        List<String> flags = builder.getFlags();

        if (versionManifest.getMinimumLauncherVersion() < 21) {
            // Add bits that the legacy manifests don't
            flags.add("-Djava.library.path=" + extractDir.getAbsoluteFile());
            flags.add("-cp");
            flags.add(builder.buildClassPath());

            if (featureList.hasFeature("has_custom_resolution")) {
                List<String> args = builder.getArgs();
                args.add("--width");
                args.add(String.valueOf(config.getWindowWidth()));
                args.add("--height");
                args.add(String.valueOf(config.getWindowHeight()));
            }

            // Add old platform hacks that the new manifests already specify
            if (getEnvironment().getPlatform() == Platform.WINDOWS) {
                flags.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
            }
        }

        if (versionManifest.getMinimumLauncherVersion() < 18) {
            // TODO find out exactly what versions need this hack.
            flags.add("-Dminecraft.applet.TargetDirectory=" + instance.getContentDir());
        }
    }

    /**
     * Build the list of command substitutions.
     *
     * @return the map of substitutions
     * @throws JsonProcessingException on error
     */
    private Map<String, String> getCommandSubstitutions() throws JsonProcessingException {
        Map<String, String> map = new HashMap<String, String>();

        map.put("version_name", versionManifest.getId());
        map.put("version_type", launcher.getProperties().getProperty("launcherShortname"));

        map.put("auth_access_token", session.getAccessToken());
        map.put("auth_session", session.getSessionToken());
        map.put("auth_player_name", session.getName());
        map.put("auth_uuid", session.getUuid());

        map.put("profile_name", session.getName());
        map.put("user_type", session.getUserType().getName());
        map.put("user_properties", mapper.writeValueAsString(session.getUserProperties()));

        map.put("game_directory", instance.getContentDir().getAbsolutePath());
        map.put("game_assets", virtualAssetsDir.getAbsolutePath());
        map.put("assets_root", launcher.getAssets().getDir().getAbsolutePath());
        map.put("assets_index_name", versionManifest.getAssetId());

        map.put("resolution_width", String.valueOf(config.getWindowWidth()));
        map.put("resolution_height", String.valueOf(config.getWindowHeight()));

        map.put("launcher_name", launcher.getTitle());
        map.put("launcher_version", launcher.getVersion());
        map.put("classpath", builder.buildClassPath());
        map.put("natives_directory", extractDir.getAbsolutePath());

        // Forge additions
        map.put("library_directory", launcher.getLibrariesDir().getAbsolutePath());
        map.put("classpath_separator", System.getProperty("path.separator"));

        return map;
    }

    @Override
    public double getProgress() {
        return progress.getProgress();
    }

    @Override
    public String getStatus() {
        return progress.getStatus();
    }

}
