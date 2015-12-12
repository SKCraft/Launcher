/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.controller.task;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.skcraft.launcher.util.MorePaths;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;

public class DirectoryWalker implements Callable<List<File>> {

    @Getter private final File dir;
    @Getter @Setter private FileFilter fileFilter = pathname -> true;
    @Getter @Setter private boolean recursive;

    public DirectoryWalker(File dir) {
        checkNotNull(dir, "dir");
        this.dir = dir;
    }

    @Override
    public List<File> call() throws IOException {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException(dir.getAbsolutePath() + " is not a directory");
        }

        List<File> matched = Lists.newArrayList();
        Set<String> seen = Sets.newHashSet();

        Queue<File> queue = new LinkedList<>();
        queue.add(dir);

        File cur;
        while ((cur = queue.poll()) != null) {
            String canonical = cur.getCanonicalPath();
            if (!seen.contains(canonical) && MorePaths.isSubDirectory(dir, cur)) {
                File[] files = cur.listFiles();

                if (files != null) {
                    for (File file : files) {
                        if (recursive && file.isDirectory()) {
                            queue.add(file);
                        }

                        if (fileFilter.accept(file)) {
                            matched.add(file);
                        }
                    }
                }
            }

        }

        return matched;
    }

}
