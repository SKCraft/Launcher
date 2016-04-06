/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.util;

import lombok.Data;

/**
 * Represents information about the current environment.
 */
@Data
public class Environment {

    private final Platform platform;
    private final String platformVersion;
    private final String arch;

    /**
     * Get an instance of the current environment.
     *
     * @return the current environment
     */
    public static Environment getInstance() {
        return new Environment(detectPlatform(), System.getProperty("os.version"), System.getProperty("os.arch"));
    }

    public String getArchBits() {
        return arch.contains("64") ? "64" : "32";
    }

    /**
     * Detect the current platform.
     *
     * @return the current platform
     */
    public static Platform detectPlatform() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win"))
            return Platform.WINDOWS;
        if (osName.contains("mac"))
            return Platform.MAC_OS_X;
        if (osName.contains("solaris") || osName.contains("sunos"))
            return Platform.SOLARIS;
        if (osName.contains("linux"))
            return Platform.LINUX;
        if (osName.contains("unix"))
            return Platform.LINUX;
        if (osName.contains("bsd"))
            return Platform.LINUX;

        return Platform.UNKNOWN;
    }

}
