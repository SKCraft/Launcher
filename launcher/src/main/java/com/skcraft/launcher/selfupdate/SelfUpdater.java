/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.selfupdate;

import com.skcraft.concurrency.DefaultProgress;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.install.FileMover;
import com.skcraft.launcher.install.Installer;
import com.skcraft.launcher.util.SharedLocale;
import lombok.NonNull;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelfUpdater implements Callable<File>, ProgressObservable {

    private final Launcher launcher;
    private final URL url;
    private final Installer installer;
    private ProgressObservable progress = new DefaultProgress(0, SharedLocale.tr("updater.updating"));

    public SelfUpdater(@NonNull Launcher launcher, @NonNull URL url) {
        this.launcher = launcher;
        this.url = url;
        this.installer = new Installer(launcher.getInstallerDir());
    }

    @Override
    public File call() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            File dir = launcher.getLauncherBinariesDir();
            File file = new File(dir, String.valueOf(System.currentTimeMillis()) + ".jar.pack");
            File tempFile = installer.getDownloader().download(url, "", 10000, "launcher.jar.pack");

            progress = installer.getDownloader();
            installer.download();

            installer.queue(new FileMover(tempFile, file));

            progress = installer;
            installer.execute();

            return file;
        } finally {
            executor.shutdownNow();
        }
    }

    @Override
    public double getProgress() {
        return progress.getProgress();
    }

    @Override
    public String getStatus() {
        return progress.getStatus();
    }

}
