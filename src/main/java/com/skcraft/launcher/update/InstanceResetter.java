/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.update;

import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.LauncherUtils;
import com.skcraft.launcher.persistence.Persistence;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

public class InstanceResetter implements Callable<Instance>, ProgressObservable {

    private final Instance instance;
    private File currentDir;

    public InstanceResetter(@NonNull Instance instance) {
        this.instance = instance;
    }

    @Override
    public double getProgress() {
        return -1;
    }

    @Override
    public Instance call() throws Exception {
        instance.setInstalled(false);
        instance.setUpdatePending(true);
        Persistence.commitAndForget(instance);

        new File(instance.getDir(), "update_cache.json").delete();

        removeDir(new File(instance.getContentDir(), "config"));
        removeDir(new File(instance.getContentDir(), "mods"));
        
        return instance;
    }

    private void removeDir(File dir) throws IOException, InterruptedException {
        try {
            if (dir.isDirectory()) {
                currentDir = dir;
                LauncherUtils.interruptibleDelete(dir);
            }
        } finally {
            currentDir = null;
        }
    }

    public String toString() {
        File dir = currentDir;
        if (dir != null) {
            return "Removing " + dir.getAbsolutePath();
        } else {
            return "Working...";
        }
    }

}
