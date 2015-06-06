/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.install;

import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.util.SharedLocale;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.skcraft.launcher.LauncherUtils.checkInterrupted;
import static com.skcraft.launcher.util.SharedLocale.tr;

@Log
public class Installer implements ProgressObservable {

    @Getter private final File tempDir;
    private final HttpDownloader downloader;
    private InstallTask running;
    private int count = 0;
    private int finished = 0;

    private List<InstallTask> queue = new ArrayList<InstallTask>();

    public Installer(@NonNull File tempDir) {
        this.tempDir = tempDir;
        this.downloader = new HttpDownloader(tempDir);
    }

    public synchronized void queue(@NonNull InstallTask runnable) {
        queue.add(runnable);
        count++;
    }

    public void download() throws IOException, InterruptedException {
        downloader.execute();
    }

    public synchronized void execute() throws Exception {
        queue = Collections.unmodifiableList(queue);

        try {
            for (InstallTask runnable : queue) {
                checkInterrupted();
                running = runnable;
                runnable.execute();
                finished++;
            }
        } finally {
            running = null;
        }
    }

    public Downloader getDownloader() {
        return downloader;
    }

    @Override
    public double getProgress() {
        return finished / (double) count;
    }

    @Override
    public String getStatus() {
        InstallTask running = this.running;
        if (running != null) {
            String status = running.getStatus();
            if (status == null) {
                status = running.toString();
            }
            return tr("installer.executing", count - finished) + "\n" + status;
        } else {
            return SharedLocale.tr("installer.installing");
        }
    }
}
