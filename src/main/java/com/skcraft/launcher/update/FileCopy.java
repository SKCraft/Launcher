/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.update;

import com.google.common.io.Files;
import lombok.Data;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

@Data
@Log
public class FileCopy implements Runnable {

    public static final Object driveAccessLock = new Object();

    private final File from;
    private final File to;

    public FileCopy(File from, File to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public void run() {
        synchronized (driveAccessLock) {
            log.log(Level.INFO, "Copying to {0} (from {1})...", new Object[]{to.getAbsoluteFile(), from.getName()});
            try {
                to.getParentFile().mkdirs();
                Files.copy(from, to);
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy to " + to, e);
            }
        }
    }
}
