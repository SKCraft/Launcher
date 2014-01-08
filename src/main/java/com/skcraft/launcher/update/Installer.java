/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.update;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.skcraft.launcher.util.HttpRequest;
import com.skcraft.launcher.persistence.Persistence;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;
import static com.skcraft.launcher.LauncherUtils.checkInterrupted;

@Log
public class Installer {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HashFunction hf = Hashing.sha1();
    private final ListeningExecutorService executor;
    @Getter private final File temporaryDir;
    @Getter private final File dataFilesDir;
    @Getter private final File destinationDir;
    @Getter private final InstallLog currentLog = new InstallLog();
    @Getter private final InstallLog previousLog;
    @Getter private final UpdateCache updateCache;
    @Getter @Setter private int downloadTries = 5;
    @Getter @Setter private int tryDelay = 3000;
    private final List<HttpRequest> httpRequests = new ArrayList<HttpRequest>();
    private final Set<String> usedHashes = new HashSet<String>();
    private final File installLogPath;
    private final File updateCachePath;
    private final Set<Runnable> running = new HashSet<Runnable>();
    private final ErrorHandler errorHandler = new ErrorHandler();
    private Throwable throwable;

    public Installer(ListeningExecutorService executor, File temporaryDir, File dataFilesDir, File destinationDir) {
        this.executor = executor;
        this.temporaryDir = temporaryDir;
        this.dataFilesDir = dataFilesDir;
        this.destinationDir = destinationDir;

        installLogPath = new File(dataFilesDir, "install_log.json");
        updateCachePath = new File(dataFilesDir, "update_cache.json");

        this.previousLog = Persistence.read(installLogPath, InstallLog.class);
        this.updateCache = Persistence.read(updateCachePath, UpdateCache.class);
    }

    public File download(URL url, String version) throws IOException, InterruptedException {
        String baseId = hf.newHasher()
                .putString(url.toString(), Charsets.UTF_8)
                .putString(version, Charsets.UTF_8)
                .hash()
                .toString();
        String id = baseId;
        int index = 0;

        while (usedHashes.contains(id)) {
            id = baseId + "_" + (index++);
        }

        File dir = new File(temporaryDir, id.charAt(0) + File.separator + id.charAt(1));
        dir.mkdirs();
        File downloadPath = new File(dir, id + ".filepart");
        File tempPath = new File(dir, id + ".filedownload");

        if (tempPath.exists()) {
            log.log(Level.INFO, "Using existing {0} for {1}...", new Object[]{tempPath, url});
            return tempPath;
        } else {
            log.log(Level.INFO, "Downloading {0} to {1}...", new Object[]{url, downloadPath});

            int trial = 0;
            while (true) {
                HttpRequest request = HttpRequest.get(url);
                try {
                    synchronized (this) {
                        httpRequests.add(request);
                    }
                    request.execute()
                            .expectResponseCode(200)
                            .saveContent(downloadPath);
                    break;
                } catch (IOException e) {
                    if (++trial >= downloadTries) {
                        throw e;
                    }

                    log.log(Level.WARNING, String.format("Download of %s failed; retrying in %d ms", url, tryDelay), e);
                    Thread.sleep(tryDelay);
                } finally {
                    synchronized (this) {
                        httpRequests.remove(request);
                    }
                }
            }

            downloadPath.renameTo(tempPath);
            return tempPath;
        }
    }

    public synchronized ListenableFuture<?> submit(Runnable runnable) {
        running.add(runnable);
        ListenableFuture<?> future = executor.submit(runnable);
        Futures.addCallback(future, errorHandler);
        future.addListener(new RemoveRunnable(runnable), sameThreadExecutor());
        return future;
    }

    public synchronized void submitAll(List<? extends Runnable> tasks) {
        for (Runnable runnable : tasks) {
            submit(runnable);
        }
    }

    private synchronized void failAll(Throwable throwable) {
        this.throwable = throwable;
        running.clear();
        executor.shutdownNow();
        notifyAll();
    }

    public void awaitCompletion() throws ExecutionException, InterruptedException {
        try {
            synchronized (this) {
                while (running.size() > 0) {
                    wait();
                }
            }

            if (throwable != null) {
                throw new ExecutionException(throwable);
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            throw new InterruptedException();
        }
    }

    public void commit() throws IOException, InterruptedException {
        deleteOldFiles();
        writeCache();
    }

    protected void deleteOldFiles() throws InterruptedException {
        for (Map.Entry<String, Set<String>> entry : previousLog.getEntrySet()) {
            for (String path : entry.getValue()) {
                checkInterrupted();
                if (!currentLog.has(path)) {
                    new File(getDestinationDir(), path).delete();
                }
            }
        }
    }

    protected void writeCache() throws IOException {
        Persistence.write(installLogPath, currentLog);
        Persistence.write(updateCachePath, updateCache);
    }

    private class RemoveRunnable implements Runnable {
        private final Runnable runnable;

        public RemoveRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            synchronized (Installer.this) {
                running.remove(runnable);
                if (running.isEmpty()) {
                    Installer.this.notifyAll();
                }
            }
        }
    }

    private class ErrorHandler implements FutureCallback<Object> {
        @Override
        public void onSuccess(Object result) {
        }

        @Override
        public void onFailure(Throwable t) {
            if (t instanceof InterruptedException) {
                return;
            }
            log.log(Level.WARNING, "Failed install stage", t);
            failAll(t);
        }
    }


    @Override
    public synchronized String toString() {
        StringBuilder builder = new StringBuilder();

        if (httpRequests.size() > 0) {
            builder.append("Downloads:\n");
            for (HttpRequest request : httpRequests) {
                builder.append("- ");
                builder.append(request.getUrl());
                builder.append(" (");
                double progress = request.getProgress();
                if (progress >= 0) {
                    builder.append(Math.round(request.getProgress() * 100.0 * 100.0) / 100.0);
                    builder.append("%)");
                } else {
                    builder.append("pending)");
                }
                builder.append("\n");
            }
            builder.append("\n");

        }

        builder.append("Tasks:\n");
        for (Runnable runnable : running) {
            builder.append("- ");
            builder.append(runnable.toString());
            builder.append("\n");
        }
        return builder.toString();
    }

}
