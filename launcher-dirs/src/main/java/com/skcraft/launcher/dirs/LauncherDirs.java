package com.skcraft.launcher.dirs;

import java.nio.file.Path;

public record LauncherDirs(Path dataDir, Path configDir, Path cacheDir) {
    /**
     * Create a LauncherDirs that stores all data in a single root folder.
     *
     * @param singleDir The data folder for this launcher instance.
     */
    public static LauncherDirs singleFolder(Path singleDir) {
        return new LauncherDirs(singleDir, singleDir, singleDir.resolve("temp"));
    }

    /**
     * Get the directory containing the instances.
     *
     * @return the instances dir
     */
    public Path getInstancesDir() {
        return dataDir.resolve("instances");
    }

    public Path getLibrariesDir() {
        return dataDir.resolve("libraries");
    }

    public Path getVersionsDir() {
        return dataDir.resolve("versions");
    }

    /**
     * Get the directory containing the launcher JARs (us!)
     */
    public Path getLauncherDir() {
        return dataDir.resolve("launcher");
    }

    /**
     * Get the directory to store temporary install files.
     *
     * @return the temporary install directory
     */
    public Path getInstallerDir() {
        return cacheDir.resolve("install");
    }

    /**
     * Get the directory to store temporarily extracted files.
     *
     * @return the directory
     */
    public Path getExtractDir() {
        return cacheDir.resolve("extract");
    }
}
