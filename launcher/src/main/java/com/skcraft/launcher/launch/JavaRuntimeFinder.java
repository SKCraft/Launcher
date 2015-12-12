/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.launch;

import com.skcraft.launcher.util.Environment;
import com.skcraft.launcher.util.Platform;
import com.skcraft.launcher.util.WinRegistry;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Finds the best Java runtime to use.
 */
public final class JavaRuntimeFinder {

    private JavaRuntimeFinder() {
    }

    /**
     * Return the path to the best found JVM location.
     *
     * @return the JVM location, or null
     */
    public static File findBestJavaPath() {
        if (Environment.getInstance().getPlatform() != Platform.WINDOWS) {
            return null;
        }
        
        List<JREEntry> entries = new ArrayList<JREEntry>();
        try {
            getEntriesFromRegistry(entries, "SOFTWARE\\JavaSoft\\Java Runtime Environment");
            getEntriesFromRegistry(entries, "SOFTWARE\\JavaSoft\\Java Development Kit");
        } catch (Throwable ignored) {
        }
        Collections.sort(entries);
        
        if (entries.size() > 0) {
            return new File(entries.get(0).dir, "bin");
        }
        
        return null;
    }
    
    private static void getEntriesFromRegistry(List<JREEntry> entries, String basePath)
            throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        List<String> subKeys = WinRegistry.readStringSubKeys(WinRegistry.HKEY_LOCAL_MACHINE, basePath);
        for (String subKey : subKeys) {
            JREEntry entry = getEntryFromRegistry(basePath, subKey);
            if (entry != null) {
                entries.add(entry);
            }
        }
    }
    
    private static JREEntry getEntryFromRegistry(String basePath, String version)  throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        String regPath = basePath + "\\" + version;
        String path = WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE, regPath, "JavaHome");
        File dir = new File(path);
        if (dir.exists() && new File(dir, "bin/java.exe").exists()) {
            JREEntry entry = new JREEntry();
            entry.dir = dir;
            entry.version = version;
            entry.is64Bit = guessIf64Bit(dir);
            return entry;
        } else {
            return null;
        }
    }
    
    private static boolean guessIf64Bit(File path) {
        try {
            String programFilesX86 = System.getenv("ProgramFiles(x86)");
            return programFilesX86 == null || !path.getCanonicalPath().startsWith(new File(programFilesX86).getCanonicalPath());
        } catch (IOException ignored) {
            return false;
        }
    }
    
    private static class JREEntry implements Comparable<JREEntry> {
        private File dir;
        private String version;
        private boolean is64Bit;

        @Override
        public int compareTo(JREEntry o) {
            if (is64Bit && !o.is64Bit) {
                return -1;
            } else if (!is64Bit && o.is64Bit) {
                return 1;
            }
            
            String[] a = version.split("[\\._]");
            String[] b = o.version.split("[\\._]");
            int min = Math.min(a.length, b.length);
            
            for (int i = 0; i < min; i++) {
                int first, second;
                
                try {
                    first = Integer.parseInt(a[i]);
                } catch (NumberFormatException e) {
                    return -1;
                }
                
                try {
                    second = Integer.parseInt(b[i]);
                } catch (NumberFormatException e) {
                    return 1;
                }
                
                if (first > second) {
                    return -1;
                } else if (first < second) {
                    return 1;
                }
            }
            
            if (a.length == b.length) {
                return 0; // Same
            }
            
            return a.length > b.length ? -1 : 1;
        }
    }

}
