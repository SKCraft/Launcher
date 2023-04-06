/*
 * SKCraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.update;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.dialog.ProgressDialog;
import com.skcraft.launcher.selfupdate.LatestVersionInfo;
import com.skcraft.launcher.selfupdate.SelfUpdater;
import com.skcraft.launcher.selfupdate.UpdateChecker;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;
import lombok.Getter;

import javax.swing.*;
import javax.swing.event.SwingPropertyChangeSupport;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;

public class UpdateManager {

    @Getter
    private final SwingPropertyChangeSupport propertySupport = new SwingPropertyChangeSupport(this);
    private final Launcher launcher;
    private LatestVersionInfo pendingUpdate;

    public UpdateManager(Launcher launcher) {
        this.launcher = launcher;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    public boolean getPendingUpdate() {
        return pendingUpdate != null;
    }

    public void checkForUpdate(final Window window) {
        ListenableFuture<LatestVersionInfo> future = launcher.getExecutor().submit(new UpdateChecker(launcher));

        Futures.addCallback(future, new FutureCallback<LatestVersionInfo>() {
            @Override
            public void onSuccess(LatestVersionInfo result) {
                if (result != null) {
                    requestUpdate(result);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // Error handler attached below.
            }
        }, SwingExecutor.INSTANCE);

        SwingHelper.addErrorDialogCallback(window, future);
    }

    public void performUpdate(final Window window) {
        final URL url = pendingUpdate.getUrl();

        if (url != null) {
            SelfUpdater downloader = new SelfUpdater(launcher, url);
            ObservableFuture<File> future = new ObservableFuture<File>(
                    launcher.getExecutor().submit(downloader), downloader);

            Futures.addCallback(future, new FutureCallback<File>() {
                @Override
                public void onSuccess(File result) {
                    propertySupport.firePropertyChange("pendingUpdate", true, false);
                    UpdateManager.this.pendingUpdate = null;

                    SwingHelper.showMessageDialog(
                            window,
                            SharedLocale.tr("launcher.selfUpdateComplete"),
                            SharedLocale.tr("launcher.selfUpdateCompleteTitle"),
                            null,
                            JOptionPane.INFORMATION_MESSAGE);
                }

                @Override
                public void onFailure(Throwable t) {
                }
            }, SwingExecutor.INSTANCE);

            ProgressDialog.showProgress(window, future, SharedLocale.tr("launcher.selfUpdatingTitle"), SharedLocale.tr("launcher.selfUpdatingStatus"));
            SwingHelper.addErrorDialogCallback(window, future);
        } else {
            propertySupport.firePropertyChange("pendingUpdate", false, false);
        }
    }

    private void requestUpdate(LatestVersionInfo url) {
        propertySupport.firePropertyChange("pendingUpdate", getPendingUpdate(), url != null);
        this.pendingUpdate = url;
    }


}
