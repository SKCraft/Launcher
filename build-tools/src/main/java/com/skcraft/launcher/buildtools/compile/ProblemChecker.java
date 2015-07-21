/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.buildtools.compile;

import com.beust.jcommander.internal.Lists;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.builder.BuilderOptions;
import com.skcraft.launcher.buildtools.BuildTools;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

public class ProblemChecker implements Callable<List<Problem>>, ProgressObservable {

    private final BuildTools buildTools;
    private String status = "Checking for problems...";

    public ProblemChecker(BuildTools buildTools) {
        this.buildTools = buildTools;
    }

    @Override
    public List<Problem> call() throws Exception {
        List<Problem> problems = Lists.newArrayList();

        File inputDir = buildTools.getInputDir();
        File srcDir = buildTools.getSrcDir();
        File loadersDir = new File(inputDir, BuilderOptions.DEFAULT_LOADERS_DIRNAME);
        File modsDir = new File(srcDir, "mods");
        boolean hasLoaders = hasFiles(loadersDir);
        boolean hasMods = hasFiles(modsDir);

        String[] files;

        if (new File(inputDir, "_CLIENT").exists()) {
            problems.add(new Problem("Root _CLIENT", "There's a _CLIENT folder that's not in " +
                    "the src/ folder. Only files that are in src/ will actually appear in the " +
                    "modpack, so you probably intended to put _CLIENT in src/."));
        }

        if (new File(inputDir, "_SERVER").exists()) {
            problems.add(new Problem("Root _SERVER", "There's a _SERVER folder that's not in " +
                    "the src/ folder. Only files that are in src/ will actually appear in the " +
                    "modpack, so you probably intended to put _SERVER in src/."));
        }

        if (new File(inputDir, "mods").exists()) {
            problems.add(new Problem("Root mods", "There's a mods folder that's not in " +
                    "the src/ folder. Only files that are in src/ will actually appear in the " +
                    "modpack."));
        }

        if (new File(inputDir, "config").exists()) {
            problems.add(new Problem("Root mods", "There's a config folder that's not in " +
                    "the src/ folder. Only files that are in src/ will actually appear in the " +
                    "modpack."));
        }

        if (new File(inputDir, "version.json").exists()) {
            problems.add(new Problem("Legacy version.json", "There's a version.json file in the " +
                    "project directory. If you are upgrading your modpack from an old version " +
                    "of the launcher, then you should be able to delete version.json as it is " +
                    "no longer needed to create a modpack. If you are intentionally overriding the " +
                    "Minecraft version manifest, then ignore this warning."));
        }

        if (hasMods && !hasLoaders) {
            problems.add(new Problem("No Loaders", "There appears to be a mods/ folder but there's no mod loaders in loaders/."));
        }

        return problems;
    }

    @Override
    public double getProgress() {
        return -1;
    }

    @Override
    public String getStatus() {
        return status;
    }

    private static boolean hasFiles(File dir) {
        String[] contents = dir.list();
        return contents != null && contents.length > 0;
    }

}
