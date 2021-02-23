/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.bootstrap;

import lombok.Getter;
import lombok.extern.java.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
public class LauncherBinary implements Comparable<LauncherBinary> {

    public static final Pattern PATTERN = Pattern.compile("^([0-9]+)\\.jar(\\.pack)?$");
    @Getter
    private final File path;
    private final long time;
    private final boolean packed;

    public LauncherBinary(File path) {
        this.path = path;
        String name = path.getName();
        Matcher m = PATTERN.matcher(name);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid filename: " + path);
        }
        time = Long.parseLong(m.group(1));
        packed = m.group(2) != null;
    }

    public File getExecutableJar() throws PackedJarException {
        if (packed) {
            log.warning("Launcher binary " + path.getAbsolutePath() + " is a pack200 file, which is " +
                    "no longer supported.");

            throw new PackedJarException("Cannot unpack .jar.pack files!");
        } else {
            return path;
        }
    }

    @Override
    public int compareTo(LauncherBinary o) {
        if (time > o.time) {
            return -1;
        } else if (time < o.time) {
            return 1;
        } else {
            if (packed && !o.packed) {
                return 1;
            } else if (!packed && o.packed) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public void remove() {
        path.delete();
    }

    public static class Filter implements FileFilter {
        @Override
        public boolean accept(File file) {
            return file.isFile() && LauncherBinary.PATTERN.matcher(file.getName()).matches();
        }
    }

    public static class PackedJarException extends Exception {
        public PackedJarException(String message) {
            super(message);
        }
    }
}
