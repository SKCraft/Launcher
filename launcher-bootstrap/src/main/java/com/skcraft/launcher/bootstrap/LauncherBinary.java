/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.bootstrap;

import lombok.Getter;
import lombok.extern.java.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
public class LauncherBinary implements Comparable<LauncherBinary> {

    public static final Pattern PATTERN = Pattern.compile("^([0-9]+)\\.jar(\\.pack)?$");
    @Getter
    private final Path path;
    private final long time;
    private final boolean packed;

    public LauncherBinary(Path path) {
        this.path = path;

        String name = path.getFileName().toString();
        Matcher m = PATTERN.matcher(name);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid filename: " + path);
        }

        time = Long.parseLong(m.group(1));
        packed = m.group(2) != null;
    }

    public Path getExecutableJar() throws PackedJarException {
        if (packed) {
            log.warning("Launcher binary " + path.toAbsolutePath() + " is a pack200 file, which is " +
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

    public void remove() throws IOException {
        Files.delete(path);
    }

    public static class Filter implements BiPredicate<Path, BasicFileAttributes> {
        @Override
        public boolean test(Path path, BasicFileAttributes basicFileAttributes) {
            return Files.isRegularFile(path) && PATTERN.matcher(path.getFileName().toString()).matches();
        }
    }

    public static class PackedJarException extends Exception {
        public PackedJarException(String message) {
            super(message);
        }
    }
}
