/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.controller.task;

import com.beust.jcommander.internal.Lists;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.LauncherException;
import com.skcraft.launcher.LauncherUtils;
import com.skcraft.launcher.builder.PackageBuilder;
import com.skcraft.launcher.creator.Creator;
import com.skcraft.launcher.creator.model.creator.Pack;
import com.skcraft.launcher.creator.plugin.CreatorToolsPlugin;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
public class PackBuilder implements Callable<PackBuilder>, ProgressObservable {

    private final Creator creator;
    private final Pack pack;
    private final File outputDir;
    private final String version;
    private final String manifestFilename;
    private final boolean clean;
    private final boolean downloadUrls;

    @Override
    public PackBuilder call() throws Exception {
        if (clean) {
            List<File> failures = new ArrayList<File>();

            try {
                LauncherUtils.interruptibleDelete(outputDir, failures);
            } catch (IOException e) {
                Thread.sleep(1000);
                LauncherUtils.interruptibleDelete(outputDir, failures);
            }

            if (failures.size() > 0) {
                throw new LauncherException(failures.size() + " failed to delete", "There were " + failures.size() + " failures during cleaning.");
            }
        }

        //noinspection ResultOfMethodCallIgnored
        outputDir.mkdirs();

        System.setProperty("com.skcraft.builder.ignoreURLOverrides", downloadUrls ? "false" : "true");
        List<String> args = Lists.newArrayList(
                "--version", version,
                "--manifest-dest", new File(outputDir, manifestFilename).getAbsolutePath(),
                "-i", pack.getDirectory().getAbsolutePath(),
                "-o", outputDir.getAbsolutePath()
        );

        for (CreatorToolsPlugin plugin : creator.getPlugins()) {
            if (plugin.getBuilderPlugin() != null) {
                args.add("--plugin");
                args.add(plugin.getBuilderPlugin().getCanonicalName());
            }
        }

        PackageBuilder.main(args.toArray(new String[0]));

        return this;
    }

    @Override
    public double getProgress() {
        return -1;
    }

    @Override
    public String getStatus() {
        return "Building modpack...";
    }
}
