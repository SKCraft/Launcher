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
    private URL pendingUpdateUrl;

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
        return pendingUpdateUrl != null;
    }

    public void checkForUpdate() {
        ListenableFuture<URL> future = launcher.getExecutor().submit(new UpdateChecker(launcher));

        Futures.addCallback(future, new FutureCallback<URL>() {
            @Override
            public void onSuccess(URL result) {
                if (result != null) {
                    requestUpdate(result);
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        }, SwingExecutor.INSTANCE);
    }

    public void performUpdate(final Window window) {
        final URL url = pendingUpdateUrl;

        if (url != null) {
            SelfUpdater downloader = new SelfUpdater(launcher, url);
            ObservableFuture<File> future = new ObservableFuture<File>(
                    launcher.getExecutor().submit(downloader), downloader);

            Futures.addCallback(future, new FutureCallback<File>() {
                @Override
                public void onSuccess(File result) {
                    propertySupport.firePropertyChange("pendingUpdate", true, false);
                    UpdateManager.this.pendingUpdateUrl = null;

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

    private void requestUpdate(URL url) {
        propertySupport.firePropertyChange("pendingUpdate", getPendingUpdate(), url != null);
        this.pendingUpdateUrl = url;
    }


}
