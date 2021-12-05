/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.util;

import com.google.common.collect.ImmutableList;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import java.util.List;

/**
 * Windows registry helper via JNA platform
 */
public class WinRegistry {
    private WinRegistry() {
    }

    /**
     * Read a value from a path and value name
     * 
     * @param hkey Hive key
     * @param key Registry path
     * @param valueName Value key
     * @return String value from the registry
     * @throws com.sun.jna.platform.win32.Win32Exception If an error occurs accessing the registry
     *  or the path does not exist
     */
    public static String readString(WinReg.HKEY hkey, String key, String valueName) {
        return Advapi32Util.registryGetStringValue(hkey, key, valueName);
    }

    /**
     * Get the subkeys of a given path
     *
     * @param hkey Hive key
     * @param key Registry path
     * @return the subkeys of a given path
     *
     * @throws com.sun.jna.platform.win32.Win32Exception If an error occurs accessing the registry
     *  or the path does not exist
     */
    public static List<String> readStringSubKeys(WinReg.HKEY hkey, String key) {
        return ImmutableList.copyOf(Advapi32Util.registryGetKeys(hkey, key));
    }
}
