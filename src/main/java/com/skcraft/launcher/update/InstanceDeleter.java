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

import java.io.IOException;
import java.util.concurrent.Callable;

import static com.skcraft.launcher.LauncherUtils.checkInterrupted;

public class InstanceDeleter implements Callable<Instance>, ProgressObservable {

    private final Instance instance;

    public InstanceDeleter(@NonNull Instance instance) {
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

        checkInterrupted();

        Thread.sleep(2000);

        try {
            LauncherUtils.interruptibleDelete(instance.getDir());
        } catch (IOException e) {
            Thread.sleep(1000);
            LauncherUtils.interruptibleDelete(instance.getDir());
        }

        return instance;
    }

}
