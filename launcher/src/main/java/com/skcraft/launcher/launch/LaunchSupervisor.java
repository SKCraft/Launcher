/*
 * SKCraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.launch;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.Session;
import com.skcraft.launcher.dialog.LoginDialog;
import com.skcraft.launcher.dialog.ProgressDialog;
import com.skcraft.launcher.launch.LaunchOptions.UpdatePolicy;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.update.Updater;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;

import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;
import static com.skcraft.launcher.util.SharedLocale.tr;

@Log
public class LaunchSupervisor {

    private final Launcher launcher;

    public LaunchSupervisor(Launcher launcher) {
        this.launcher = launcher;
    }

    public void launch(LaunchOptions options) {
        final Window window = options.getWindow();
        final Instance instance = options.getInstance();
        final LaunchListener listener = options.getListener();

        try {
            boolean update = options.getUpdatePolicy().isUpdateEnabled() && instance.isUpdatePending();

            // Store last access date
            Date now = new Date();
            instance.setLastAccessed(now);
            Persistence.commitAndForget(instance);

            // Perform login
            final Session session;
            if (options.getSession() != null) {
                session = options.getSession();
            } else {
                session = LoginDialog.showLoginRequest(window, launcher);
                if (session == null) {
                    return;
                }
            }

            // If we have to update, we have to update
            if (!instance.isInstalled()) {
                update = true;
            }

            if (update) {
                // Execute the updater
                Updater updater = new Updater(launcher, instance);
                updater.setOnline(options.getUpdatePolicy() == UpdatePolicy.ALWAYS_UPDATE || session.isOnline());
                ObservableFuture<Instance> future = new ObservableFuture<Instance>(
                        launcher.getExecutor().submit(updater), updater);

                // Show progress
                ProgressDialog.showProgress(window, future, SharedLocale.tr("launcher.updatingTitle"), tr("launcher.updatingStatus", instance.getTitle()));
                SwingHelper.addErrorDialogCallback(window, future);

                // Update the list of instances after updating
                future.addListener(new Runnable() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                listener.instancesUpdated();
                            }
                        });
                    }
                }, SwingExecutor.INSTANCE);

                // On success, launch also
                Futures.addCallback(future, new FutureCallback<Instance>() {
                    @Override
                    public void onSuccess(Instance result) {
                        launch(window, instance, session, listener);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                    }
                }, SwingExecutor.INSTANCE);
            } else {
                launch(window, instance, session, listener);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            SwingHelper.showErrorDialog(window, SharedLocale.tr("launcher.noInstanceError"), SharedLocale.tr("launcher.noInstanceTitle"));
        }
    }

    private void launch(Window window, Instance instance, Session session, final LaunchListener listener) {
        final File extractDir = launcher.createExtractDir();

        // Get the process
        Runner task = new Runner(launcher, instance, session, extractDir);
        ObservableFuture<Process> processFuture = new ObservableFuture<Process>(
                launcher.getExecutor().submit(task), task);

        // Show process for the process retrieval
        ProgressDialog.showProgress(
                window, processFuture, SharedLocale.tr("launcher.launchingTItle"), tr("launcher.launchingStatus", instance.getTitle()));

        // If the process is started, get rid of this window
        Futures.addCallback(processFuture, new FutureCallback<Process>() {
            @Override
            public void onSuccess(Process result) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        listener.gameStarted();
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });

        // Watch the created process
        ListenableFuture<?> future = Futures.transform(
                processFuture, new LaunchProcessHandler(launcher), launcher.getExecutor());
        SwingHelper.addErrorDialogCallback(null, future);

        // Clean up at the very end
        future.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    log.info("Process ended; cleaning up " + extractDir.getAbsolutePath());
                    FileUtils.deleteDirectory(extractDir);
                } catch (IOException e) {
                    log.log(Level.WARNING, "Failed to clean up " + extractDir.getAbsolutePath(), e);
                }

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        listener.gameClosed();
                    }
                });
            }
        }, sameThreadExecutor());
    }
}
