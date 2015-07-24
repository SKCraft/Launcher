/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.builder;

import com.beust.jcommander.internal.Lists;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class BuilderUtils {

    private static final DateFormat VERSION_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private BuilderUtils() {
    }

    public static String normalizePath(String path) {
        return path.replaceAll("^[/\\\\]*", "").replaceAll("[/\\\\]+", "/");
    }

    public static ZipEntry getZipEntry(ZipFile jarFile, String path) {
        Enumeration<? extends ZipEntry> entries = jarFile.entries();
        String expected = normalizePath(path);

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String test = normalizePath(entry.getName());
            if (expected.equals(test)) {
                return entry;
            }
        }

        return null;
    }

    public static List<Compressor> getCompressors(String repoUrl) {
        if (repoUrl.matches("^https?://files.minecraftforge.net/maven/")) {
            return Lists.newArrayList(
                    new Compressor("xz", CompressorStreamFactory.XZ),
                    new Compressor("pack", CompressorStreamFactory.PACK200));
        } else {
            return Collections.emptyList();
        }
    }

    public static String generateVersionFromDate() {
        Date today = Calendar.getInstance().getTime();
        return VERSION_DATE_FORMAT.format(today);
    }

}
