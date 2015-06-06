/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.install;

import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import static com.skcraft.launcher.util.SharedLocale.tr;

@Log
public class InstallLogFileMover implements InstallTask {

    private final InstallLog installLog;
    private final File from;
    private final File to;

    public InstallLogFileMover(InstallLog installLog, @NonNull File from, @NonNull File to) {
        this.installLog = installLog;
        this.from = from;
        this.to = to;
    }

    @Override
    public void execute() throws IOException {
        InstallLogFileMover.log.log(Level.INFO, "Installing to {0} (from {1})...", new Object[]{to.getAbsoluteFile(), from.getName()});
        to.getParentFile().mkdirs();
        to.delete();
        from.renameTo(to);
        installLog.add(to, to);
    }

    @Override
    public double getProgress() {
        return -1;
    }

    @Override
    public String getStatus() {
        return tr("installer.movingFile", from, to);
    }

}
