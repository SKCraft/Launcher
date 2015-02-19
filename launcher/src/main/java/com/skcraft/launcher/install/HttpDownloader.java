/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.install;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.util.HttpRequest;
import com.skcraft.launcher.util.SharedLocale;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import static com.skcraft.launcher.util.SharedLocale.tr;

@Log
public class HttpDownloader implements Downloader {

    private final Random random = new Random();
    private final HashFunction hf = Hashing.sha1();

    private final File tempDir;
    @Getter @Setter private int threadCount = 6;
    @Getter @Setter private int retryDelay = 2000;
    @Getter @Setter private int tryCount = 3;

    private List<HttpDownloadJob> queue = new ArrayList<HttpDownloadJob>();
    private final Set<String> usedKeys = new HashSet<String>();

    private final List<HttpDownloadJob> running = new ArrayList<HttpDownloadJob>();
    private final List<HttpDownloadJob> failed = new ArrayList<HttpDownloadJob>();
    private long downloaded = 0;
    private long total = 0;
    private int left = 0;

    /**
     * Create a new downloader using the given executor.
     *
     * @param tempDir the temporary directory
     */
    public HttpDownloader(@NonNull File tempDir) {
        this.tempDir = tempDir;
    }

    /**
     * Make sure that we aren't re-using hash IDs.
     *
     * @param baseKey the key to make unique
     * @return a unique key
     */
    private String createUniqueKey(String baseKey) {
        String key = baseKey;
        int i = 0;
        while (usedKeys.contains(key)) {
            key = baseKey + "_" + (i++);
        }
        usedKeys.add(key);
        return key;
    }

    @Override
    public synchronized File download(@NonNull List<URL> urls, @NonNull String key, long size, String name) {
        if (urls.isEmpty()) {
            throw new IllegalArgumentException("Can't download empty list of URLs");
        }

        String hash = hf.hashString(Strings.nullToEmpty(key) + urls.get(0), Charsets.UTF_8).toString();
        hash = createUniqueKey(hash);
        File tempFile = new File(tempDir, hash.substring(0, 2) + "/" + hash);

        // If the file is already downloaded (such as from before), then don't re-download
        if (!tempFile.exists()) {
            total += size;
            left++;
            queue.add(new HttpDownloadJob(tempFile, urls, size, name != null ? name : tempFile.getName()));
        }

        return tempFile;
    }


    @Override
    public File download(URL url, String key, long size, String name) {
        List<URL> urls = new ArrayList<URL>();
        urls.add(url);
        return download(urls, key, size, name);
    }

    /**
     * Prevent further downloads from being queued and download queued files.
     *
     * @throws InterruptedException thrown on interruption
     * @throws IOException thrown on I/O error
     */
    public void execute() throws InterruptedException, IOException {
        synchronized (this) {
            queue = Collections.unmodifiableList(queue);
        }

        ListeningExecutorService executor = MoreExecutors.listeningDecorator(
                Executors.newFixedThreadPool(threadCount));

        try {
            List<ListenableFuture<?>> futures = new ArrayList<ListenableFuture<?>>();

            synchronized (this) {
                for (HttpDownloadJob job : queue) {
                    futures.add(executor.submit(job));
                }
            }

            try {
                Futures.allAsList(futures).get();
            } catch (ExecutionException e) {
                throw new IOException("Something went wrong", e);
            }

            synchronized (this) {
                if (failed.size() > 0) {
                    throw new IOException(failed.size() + " file(s) could not be downloaded");
                }
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Override
    public synchronized double getProgress() {
        if (total <= 0) {
            return -1;
        }

        long downloaded = this.downloaded;
        for (HttpDownloadJob job : running) {
            downloaded += Math.max(0, job.getProgress() * job.size);
        }
        return downloaded / (double) total;
    }

    @Override
    public synchronized String getStatus() {
        String failMessage = tr("downloader.failedCount", failed.size());
        if (running.size() == 1) {
            return tr("downloader.downloadingItem", running.get(0).getName()) +
                    "\n" + running.get(0).getStatus() +
                    "\n" + failMessage;
        } else if (running.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (HttpDownloadJob job : running) {
                builder.append("\n");
                builder.append(job.getStatus());
            }
            return tr("downloader.downloadingList", queue.size(), left, failed.size()) +
                    builder.toString() +
                    "\n" + failMessage;
        } else {
            return SharedLocale.tr("downloader.noDownloads");
        }
    }

    public class HttpDownloadJob implements Runnable, ProgressObservable {
        private final File destFile;
        private final List<URL> urls;
        private final long size;
        @Getter private String name;
        private HttpRequest request;

        private HttpDownloadJob(File destFile, List<URL> urls, long size, String name) {
            this.destFile = destFile;
            this.urls = urls;
            this.size = size;
            this.name = name;
        }

        @Override
        public void run() {
            try {
                synchronized (HttpDownloader.this) {
                    running.add(this);
                }

                download();

                synchronized (HttpDownloader.this) {
                    downloaded += size;
                }
            } catch (IOException e) {
                synchronized (HttpDownloader.this) {
                    failed.add(this);
                }
            } catch (InterruptedException e) {
                log.info("Download of " + destFile + " was interrupted");
            } finally {
                synchronized (HttpDownloader.this) {
                    left--;
                    running.remove(this);
                }
            }
        }

        private void download() throws IOException, InterruptedException {
            log.log(Level.INFO, "Downloading " + destFile + " from " + urls);

            File destDir = destFile.getParentFile();
            File tempFile = new File(destDir, destFile.getName() + ".tmp");
            destDir.mkdirs();

            // Try to download
            download(tempFile);

            destFile.delete();
            if (!tempFile.renameTo(destFile)) {
                throw new IOException(String.format("Failed to rename %s to %s", tempFile, destFile));
            }
        }

        private void download(File file) throws IOException, InterruptedException {
            int trial = 0;
            boolean first = true;
            IOException lastException = null;

            do {
                for (URL url : urls) {
                    // Sleep between each trial
                    if (!first) {
                        Thread.sleep((long) (retryDelay / 2 + (random.nextDouble() * retryDelay)));
                    }
                    first = false;

                    try {
                        request = HttpRequest.get(url);
                        request.execute().expectResponseCode(200).saveContent(file);
                        return;
                    } catch (IOException e) {
                        lastException = e;
                        log.log(Level.WARNING, "Failed to download " + url, e);
                    }
                }
            } while (++trial < tryCount);

            throw new IOException("Failed to download from " + urls, lastException);
        }

        @Override
        public double getProgress() {
            HttpRequest request = this.request;
            return request != null ? request.getProgress() : -1;
        }

        @Override
        public String getStatus() {
            double progress = getProgress();
            if (progress >= 0) {
                return tr("downloader.jobProgress", name, Math.round(progress * 100 * 100) / 100.0);
            } else {
                return tr("downloader.jobPending", name);
            }
        }
    }

}
