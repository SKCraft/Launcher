/*
 * SKCraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.launcher.dialog.ProgressDialog;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.update.HardResetter;
import com.skcraft.launcher.update.Remover;
import com.skcraft.launcher.util.SharedLocale;

import java.awt.*;

import static com.skcraft.launcher.util.SharedLocale.tr;

public class InstanceTasks {

    private final Launcher launcher;

    public InstanceTasks(Launcher launcher) {
        this.launcher = launcher;
    }

    public ObservableFuture<Instance> delete(Window window, Instance instance) {
        // Execute the deleter
        Remover resetter = new Remover(instance);
        ObservableFuture<Instance> future = new ObservableFuture<Instance>(
                launcher.getExecutor().submit(resetter), resetter);

        // Show progress
        ProgressDialog.showProgress(
                window, future, SharedLocale.tr("instance.deletingTitle"), tr("instance.deletingStatus", instance.getTitle()));
        SwingHelper.addErrorDialogCallback(window, future);

        return future;
    }

    public ObservableFuture<Instance> hardUpdate(Window window, Instance instance) {
        // Execute the resetter
        HardResetter resetter = new HardResetter(instance);
        ObservableFuture<Instance> future = new ObservableFuture<Instance>(
                launcher.getExecutor().submit(resetter), resetter);

        // Show progress
        ProgressDialog.showProgress(window, future, SharedLocale.tr("instance.resettingTitle"),
                tr("instance.resettingStatus", instance.getTitle()));
        SwingHelper.addErrorDialogCallback(window, future);

        return future;
    }

    public ObservableFuture<InstanceList> reloadInstances(Window window) {
        InstanceList.Enumerator loader = launcher.getInstances().createEnumerator();
        ObservableFuture<InstanceList> future = new ObservableFuture<InstanceList>(launcher.getExecutor().submit(loader), loader);

        ProgressDialog.showProgress(window, future, SharedLocale.tr("launcher.checkingTitle"), SharedLocale.tr("launcher.checkingStatus"));
        SwingHelper.addErrorDialogCallback(window, future);

        return future;
    }

}
