/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.builder;

import lombok.NonNull;

import java.io.File;
import java.io.IOException;

/**
 * Abstract class to recursively walk a directory, keep track of a relative
 * path (which may be modified by dropping certain directory entries),
 * and call {@link #onFile(java.io.File, String)} with each file.
 */
public abstract class DirectoryWalker {

    public enum DirectoryBehavior {
        /**
         * Continue and add the given directory to the relative path.
         */
        CONTINUE,
        /**
         * Continue but don't add the given directory to the relative path.
         */
        IGNORE,
        /**
         * Don't walk this directory.
         */
        SKIP
    }

    /**
     * Walk the given directory.
     *
     * @param dir the directory
     * @throws IOException thrown on I/O error
     */
    public final void walk(@NonNull File dir) throws IOException {
        walk(dir, "");
    }

    /**
     * Recursively walk the given directory and keep track of the relative path.
     *
     * @param dir the directory
     * @param basePath the base path
     * @throws IOException
     */
    private void walk(@NonNull File dir, @NonNull String basePath) throws IOException {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException(dir.getAbsolutePath() + " is not a directory");
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String newPath = basePath;

                    switch (getBehavior(file.getName())) {
                        case CONTINUE:
                            newPath += file.getName() + "/";
                        case IGNORE:
                            walk(file, newPath);
                            break;
                        case SKIP: break;
                    }
                } else {
                    onFile(file, basePath + file.getName());
                }
            }
        }
    }

    /**
     * Return the behavior for the given directory name.
     *
     * @param name the directory name
     * @return the behavor
     */
    protected DirectoryBehavior getBehavior(String name) {
        return DirectoryBehavior.CONTINUE;
    }

    /**
     * Callback on each file.
     *
     * @param file the file
     * @param relPath the relative path
     * @throws IOException thrown on I/O error
     */
    protected abstract void onFile(File file, String relPath) throws IOException;


}
