/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.selfupdate;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.update.FileDownloader;
import com.skcraft.launcher.update.Installer;
import lombok.NonNull;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class LauncherUpdater implements Callable<File>, ProgressObservable {

    private final ListeningExecutorService executor =
            MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
    private final Launcher launcher;
    private final URL url;
    private Installer currentInstaller;

    public LauncherUpdater(@NonNull Launcher launcher, @NonNull URL url) {
        this.launcher = launcher;
        this.url = url;
    }

    @Override
    public File call() throws Exception {
        try {
            File dir = launcher.getLauncherBinariesDir();
            File finalPath = new File(dir, String.valueOf(System.currentTimeMillis()) + ".jar.pack");
            List<File> paths = new ArrayList<File>();
            paths.add(finalPath);

            Installer installer = new Installer(executor, launcher.getInstallerDir(), dir, dir);
            currentInstaller = installer;
            installer.submit(new FileDownloader(installer, url, paths));
            installer.awaitCompletion();

            return finalPath;
        } finally {
            executor.shutdownNow();
        }
    }

    @Override
    public double getProgress() {
        return -1;
    }

    @Override
    public String toString() {
        Installer installer = currentInstaller;
        if (installer != null) {
            return installer.toString();
        } else {
            return "...";
        }
    }

}
