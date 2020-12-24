/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.install;

import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.Launcher;
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

    private TaskQueue mainQueue = new TaskQueue();
    private TaskQueue lateQueue = new TaskQueue();

    private transient TaskQueue activeQueue;

    public Installer(@NonNull File tempDir) {
        this.tempDir = tempDir;
        this.downloader = new HttpDownloader(tempDir);
    }

    public synchronized void queue(@NonNull InstallTask runnable) {
        mainQueue.queue(runnable);
    }

    public synchronized void queueLate(@NonNull InstallTask runnable) {
        lateQueue.queue(runnable);
    }

    public void download() throws IOException, InterruptedException {
        downloader.execute();
    }

    public synchronized void execute(Launcher launcher) throws Exception {
        activeQueue = mainQueue;
        mainQueue.execute(launcher);
        activeQueue = null;
    }

    public synchronized void executeLate(Launcher launcher) throws Exception {
        activeQueue = lateQueue;
        lateQueue.execute(launcher);
        activeQueue = null;
    }

    public Downloader getDownloader() {
        return downloader;
    }

    @Override
    public double getProgress() {
        if (activeQueue == null) return 0.0;

        return activeQueue.finished / (double) activeQueue.count;
    }

    @Override
    public String getStatus() {
        if (activeQueue != null && activeQueue.running != null) {
            InstallTask running = activeQueue.running;
            String status = running.getStatus();
            if (status == null) {
                status = running.toString();
            }
            return tr("installer.executing", activeQueue.count - activeQueue.finished) + "\n" + status;
        } else {
            return SharedLocale.tr("installer.installing");
        }
    }

    public static class TaskQueue {
        private List<InstallTask> queue = new ArrayList<InstallTask>();

        private int count = 0;
        private int finished = 0;
        private InstallTask running;

        public synchronized void queue(@NonNull InstallTask runnable) {
            queue.add(runnable);
            count++;
        }

        public synchronized void execute(Launcher launcher) throws Exception {
            queue = Collections.unmodifiableList(queue);

            try {
                for (InstallTask runnable : queue) {
                    checkInterrupted();
                    running = runnable;
                    runnable.execute(launcher);
                    finished++;
                }
            } finally {
                running = null;
            }
        }
    }
}
