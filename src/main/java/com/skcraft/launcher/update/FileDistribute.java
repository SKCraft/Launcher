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
import java.util.List;
import java.util.logging.Level;

@Data
@Log
public class FileDistribute implements Runnable {

    private final File from;
    private final List<File> to;

    public FileDistribute(File from, List<File> to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public void run() {
        synchronized (FileCopy.driveAccessLock) {
            try {
                for (int i = 0; i < to.size(); i++) {
                    File dest = to.get(i);
                    dest.getParentFile().mkdirs();
                    log.log(Level.INFO, "Copying to {0} (from {1})...",
                            new Object[]{dest.getAbsoluteFile(), from.getName()});
                    if (i == to.size() - 1) {
                        dest.delete();
                        from.renameTo(dest);
                    } else {
                        Files.copy(from, dest);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy to " + to, e);
            }
        }
    }
}
