/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.controller.task;

import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.LauncherException;
import com.skcraft.launcher.LauncherUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class DirectoryDeleter implements Callable<File>, ProgressObservable {

    private final File dir;

    public DirectoryDeleter(File dir) {
        this.dir = dir;
    }

    @Override
    public File call() throws Exception {
        Thread.sleep(2000);

        List<File> failures = new ArrayList<File>();

        try {
            LauncherUtils.interruptibleDelete(dir, failures);
        } catch (IOException e) {
            Thread.sleep(1000);
            LauncherUtils.interruptibleDelete(dir, failures);
        }

        if (failures.size() > 0) {
            throw new LauncherException(failures.size() + " failed to delete", failures.size() + " file(s) could not be deleted");
        }

        return dir;
    }

    @Override
    public double getProgress() {
        return -1;
    }

    @Override
    public String getStatus() {
        return "Deleting files...";
    }

}
