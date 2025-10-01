package com.skcraft.launcher.bootstrap;

import com.skcraft.launcher.dirs.LauncherDirs;
import com.skcraft.launcher.util.Platform;
import com.sun.jna.platform.win32.KnownFolders;
import com.sun.jna.platform.win32.Shell32Util;

import java.nio.file.Path;

public class PlatformDirFinder {
    public static LauncherDirs get(Platform platform, BootstrapConfig config) {
        return switch (platform) {
            case WINDOWS -> {
                var dataDir = Path.of(Shell32Util.getKnownFolderPath(KnownFolders.FOLDERID_LocalAppData), config.appFolderWindows());
                yield LauncherDirs.singleFolder(dataDir);
            }
            case MAC_OS_X -> {
                var dataDir = Path.of(System.getProperty("user.home"), "Library", "Application Support", config.appFolderUnix());
                var cacheDir = Path.of(System.getProperty("user.home"), "Library", "Caches", config.appFolderUnix());

                yield new LauncherDirs(dataDir, dataDir, cacheDir);
            }
            case LINUX -> {
                var dataDir = getXDGDir("XDG_DATA_HOME", ".local", "share").resolve(config.appFolderUnix());
                var configDir = getXDGDir("XDG_CONFIG_HOME", ".config").resolve(config.appFolderUnix());
                var cacheDir = getXDGDir("XDG_CACHE_HOME", ".cache").resolve(config.appFolderUnix());

                yield new LauncherDirs(dataDir, configDir, cacheDir);
            }
            default ->
                    LauncherDirs.singleFolder(Path.of(System.getProperty("user.home", ".%s".formatted(config.appFolderUnix()))));
        };
    }

    public static LauncherDirs getLegacy(Platform platform, LegacyBootstrapConfig config) {
        var baseDir = switch (platform) {
            case WINDOWS ->
                    Path.of(Shell32Util.getKnownFolderPath(KnownFolders.FOLDERID_Documents), config.homeFolderWindows());
            case LINUX -> getXDGDir("XDG_DATA_HOME", ".local", "share").resolve(config.homeFolderLinux());
            default -> Path.of(System.getProperty("user.home"), config.homeFolderDefault());
        };

        return LauncherDirs.singleFolder(baseDir);
    }

    public static Path getXDGDir(String envKey, String... defaultPath) {
        var xdgDir = System.getenv(envKey);
        if (xdgDir != null && !xdgDir.isEmpty()) {
            return Path.of(xdgDir);
        }

        return Path.of(System.getProperty("user.home"), defaultPath);
    }

}
