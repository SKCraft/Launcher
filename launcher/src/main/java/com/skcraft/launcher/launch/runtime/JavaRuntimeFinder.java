/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.launch.runtime;

import com.skcraft.launcher.model.minecraft.JavaVersion;
import com.skcraft.launcher.util.Environment;
import lombok.extern.java.Log;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Finds the best Java runtime to use.
 */
@Log
public final class JavaRuntimeFinder {

    private JavaRuntimeFinder() {
    }

    /**
     * Get all available Java runtimes on the system
     * @return List of available Java runtimes sorted by newest first
     */
    public static List<JavaRuntime> getAvailableRuntimes() {
        Environment env = Environment.getInstance();
        PlatformRuntimeFinder runtimeFinder = getRuntimeFinder(env);

        if (runtimeFinder == null) {
            return Collections.emptyList();
        }

        // Add Minecraft javas
        List<JavaRuntime> mcRuntimes = MinecraftJavaFinder.scanLauncherDirectories(env,
                runtimeFinder.getLauncherDirectories(env));
        Set<JavaRuntime> entries = new HashSet<>(mcRuntimes);

        // Add system Javas
        runtimeFinder.getCandidateJavaLocations().stream()
                .map(JavaRuntimeFinder::getRuntimeFromPath)
                .forEach(entries::add);

        // Add extra runtimes
        entries.addAll(runtimeFinder.getExtraRuntimes());

        return entries.stream().sorted().collect(Collectors.toList());
    }

    /**
     * Find the best runtime for a given Java version
     * @param targetVersion Version to match
     * @return Java runtime if available, empty Optional otherwise
     */
    public static Optional<JavaRuntime> findBestJavaRuntime(JavaVersion targetVersion) {
        List<JavaRuntime> entries = getAvailableRuntimes();

        return entries.stream().sorted()
                .filter(runtime -> runtime.getMajorVersion() == targetVersion.getMajorVersion())
                .findFirst();
    }

    public static Optional<JavaRuntime> findAnyJavaRuntime() {
        return getAvailableRuntimes().stream().sorted().findFirst();
    }

    public static JavaRuntime getRuntimeFromPath(String path) {
        return getRuntimeFromPath(new File(path));
    }

    public static JavaRuntime getRuntimeFromPath(File target) {
        if (target.isFile()) {
            // Probably referring directly to bin/java, back up two levels
            target = target.getParentFile().getParentFile();
        } else if (target.getName().equals("bin")) {
            // Probably copied the bin directory that java.exe is in
            target = target.getParentFile();
        }

        {
            File jre = new File(target, "jre/release");
            if (jre.isFile()) {
                target = jre.getParentFile();
            }
        }

        JavaReleaseFile release = JavaReleaseFile.parseFromRelease(target);
        if (release == null) {
            // Make some assumptions...
            return new JavaRuntime(target, null, true);
        }

        return new JavaRuntime(target, release.getVersion(), release.isArch64Bit());
    }

    private static PlatformRuntimeFinder getRuntimeFinder(Environment env) {
        switch (env.getPlatform()) {
            case WINDOWS:
                return new WindowsRuntimeFinder();
            case MAC_OS_X:
                return new MacRuntimeFinder();
            case LINUX:
                return new LinuxRuntimeFinder();
            default:
                return null;
        }
    }
}
