/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.controller.task;

import com.beust.jcommander.internal.Lists;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.builder.BuilderOptions;
import com.skcraft.launcher.creator.model.creator.Pack;
import com.skcraft.launcher.creator.model.creator.Problem;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

public class ProblemChecker implements Callable<List<Problem>>, ProgressObservable {

    private final Pack pack;

    public ProblemChecker(Pack pack) {
        this.pack = pack;
    }

    @Override
    public List<Problem> call() throws Exception {
        List<Problem> problems = Lists.newArrayList();

        File packDir = pack.getDirectory();
        File srcDir = pack.getSourceDir();

        File loadersDir = new File(packDir, BuilderOptions.DEFAULT_LOADERS_DIRNAME);
        File modsDir = new File(srcDir, "mods");
        boolean hasLoaders = hasFiles(loadersDir);
        boolean hasMods = hasFiles(modsDir);

        String[] files;

        if (new File(packDir, "_CLIENT").exists()) {
            problems.add(new Problem("Root _CLIENT", "There's a _CLIENT folder that's not in " +
                    "the src/ folder. Only files that are in src/ will actually appear in the " +
                    "modpack, so you probably intended to put _CLIENT in src/."));
        }

        if (new File(packDir, "_SERVER").exists()) {
            problems.add(new Problem("Root _SERVER", "There's a _SERVER folder that's not in " +
                    "the src/ folder. Only files that are in src/ will actually appear in the " +
                    "modpack, so you probably intended to put _SERVER in src/."));
        }

        if (new File(packDir, "mods").exists()) {
            problems.add(new Problem("Root mods", "There's a mods folder that's not in " +
                    "the src/ folder. Only files that are in src/ will actually appear in the " +
                    "modpack."));
        }

        if (new File(packDir, "config").exists()) {
            problems.add(new Problem("Root mods", "There's a config folder that's not in " +
                    "the src/ folder. Only files that are in src/ will actually appear in the " +
                    "modpack."));
        }

        if (new File(packDir, "version.json").exists()) {
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
        return "Checking for problems...";
    }

    private static boolean hasFiles(File dir) {
        String[] contents = dir.list();
        return contents != null && contents.length > 0;
    }

}
