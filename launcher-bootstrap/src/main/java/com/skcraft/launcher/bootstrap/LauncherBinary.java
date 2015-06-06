/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.bootstrap;

import lombok.Getter;
import lombok.extern.java.Log;

import java.io.*;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.skcraft.launcher.bootstrap.BootstrapUtils.closeQuietly;

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

    public File getExecutableJar() throws IOException {
        if (packed) {
            log.log(Level.INFO, "Need to unpack " + path.getAbsolutePath());

            String packName = path.getName();
            File outputPath = new File(path.getParentFile(), packName.substring(0, packName.length() - 5));

            if (outputPath.exists()) {
                return outputPath;
            }

            FileInputStream fis = null;
            BufferedInputStream bis = null;
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            JarOutputStream jos = null;

            try {
                fis = new FileInputStream(path);
                bis = new BufferedInputStream(fis);
                fos = new FileOutputStream(outputPath);
                bos = new BufferedOutputStream(fos);
                jos = new JarOutputStream(bos);
                Pack200.newUnpacker().unpack(bis, jos);
            } finally {
                closeQuietly(jos);
                closeQuietly(bos);
                closeQuietly(fos);
                closeQuietly(bis);
                closeQuietly(fis);
            }

            path.delete();

            return outputPath;
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
}
