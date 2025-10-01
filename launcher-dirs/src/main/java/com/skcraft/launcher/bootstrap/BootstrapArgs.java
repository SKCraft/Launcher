package com.skcraft.launcher.bootstrap;

import com.skcraft.launcher.dirs.LauncherDirs;

public record BootstrapArgs(LauncherDirs launcherDirs, int version, String[] args) {
}
