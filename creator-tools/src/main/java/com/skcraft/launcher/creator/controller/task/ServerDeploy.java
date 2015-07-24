/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.controller.task;

import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.LauncherException;
import com.skcraft.launcher.LauncherUtils;
import com.skcraft.launcher.builder.ServerCopyExport;
import com.skcraft.launcher.creator.dialog.DeployServerDialog.DeployOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ServerDeploy implements Callable<ServerDeploy>, ProgressObservable {

    private final File srcDir;
    private final DeployOptions options;

    public ServerDeploy(File srcDir, DeployOptions options) {
        this.srcDir = srcDir;
        this.options = options;
    }

    @Override
    public ServerDeploy call() throws Exception {
        File modsDir = new File(options.getDestDir(), "mods");

        if (options.isCleanMods() && modsDir.isDirectory()) {
            List<File> failures = new ArrayList<File>();

            try {
                LauncherUtils.interruptibleDelete(modsDir, failures);
            } catch (IOException e) {
                Thread.sleep(1000);
                LauncherUtils.interruptibleDelete(modsDir, failures);
            }

            if (failures.size() > 0) {
                throw new LauncherException(failures.size() + " failed to delete", "There were " + failures.size() + " failures during cleaning.");
            }
        }

        String[] args = {
                "--source", srcDir.getAbsolutePath(),
                "--dest", options.getDestDir().getAbsolutePath()
        };
        ServerCopyExport.main(args);

        return this;
    }

    @Override
    public double getProgress() {
        return -1;
    }

    @Override
    public String getStatus() {
        return "Deploying server files...";
    }

}
