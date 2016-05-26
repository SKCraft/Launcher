/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.builder;

import com.beust.jcommander.JCommander;
import com.google.common.io.Files;
import com.skcraft.launcher.util.SimpleLogFormatter;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;

@Log
public class ServerCopyExport extends DirectoryWalker {

    private final File destDir;

    public ServerCopyExport(@NonNull File destDir) {
        this.destDir = destDir;
    }

    @Override
    protected DirectoryBehavior getBehavior(String name) {
        if (name.startsWith(".")) {
            return DirectoryBehavior.SKIP;
        } else if (name.equals("_SERVER")) {
            return DirectoryBehavior.IGNORE;
        } else if (name.equals("_CLIENT")) {
            return DirectoryBehavior.SKIP;
        } else {
            return DirectoryBehavior.CONTINUE;
        }
    }

    @Override
    protected void onFile(File file, String relPath) throws IOException {
        File dest = new File(destDir, relPath);

        log.info("Copying " + file.getAbsolutePath() + " to " + dest.getAbsolutePath());
        dest.getParentFile().mkdirs();
        Files.copy(file, dest);
    }

    public static void main(String[] args) throws IOException {
        SimpleLogFormatter.configureGlobalLogger();

        ServerExportOptions options = new ServerExportOptions();
        new JCommander(options, args);

        log.info("From: " + options.getSourceDir().getAbsolutePath());
        log.info("To: " + options.getDestDir().getAbsolutePath());
        ServerCopyExport task = new ServerCopyExport(options.getDestDir());
        task.walk(options.getSourceDir());
    }

}
