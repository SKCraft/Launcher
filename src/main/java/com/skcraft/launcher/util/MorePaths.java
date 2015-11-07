/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class MorePaths {

    private MorePaths() {
    }

    public static boolean isSamePath(File a, File b) throws IOException {
        return a.getCanonicalPath().equals(b.getCanonicalPath());
    }

    public static boolean isSubDirectory(File base, File child) throws IOException {
        base = base.getCanonicalFile();
        child = child.getCanonicalFile();

        File parentFile = child;
        while (parentFile != null) {
            if (base.equals(parentFile)) {
                return true;
            }

            parentFile = parentFile.getParentFile();
        }

        return false;
    }

    public static String relativize(File base, File child) {
        Path basePath = Paths.get(base.getAbsolutePath());
        Path childPath = Paths.get(child.getAbsolutePath());
        return basePath.relativize(childPath).toString();
    }

}
