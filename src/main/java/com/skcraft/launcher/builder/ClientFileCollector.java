/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.builder;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.skcraft.launcher.model.modpack.FileInstall;
import com.skcraft.launcher.model.modpack.Manifest;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;

/**
 * Walks a path and adds hashed path versions to the given
 * {@link com.skcraft.launcher.model.modpack.Manifest}.
 */
@Log
public class ClientFileCollector extends DirectoryWalker {

    private final Manifest manifest;
    private final File destDir;
    private HashFunction hf = Hashing.sha1();

    /**
     * Create a new collector.
     *
     * @param manifest the manifest
     * @param destDir  the destination directory to copy the hashed objects
     */
    public ClientFileCollector(@NonNull Manifest manifest, @NonNull File destDir) {
        this.manifest = manifest;
        this.destDir = destDir;
    }

    @Override
    public DirectoryBehavior getBehavior(@NonNull String name) {
        if (name.equals("_OPTIONAL")) {
            return DirectoryBehavior.SKIP;
        } else if (name.equals("_SERVER")) {
            return DirectoryBehavior.SKIP;
        } else if (name.equals("_CLIENT")) {
            return DirectoryBehavior.IGNORE;
        } else {
            return DirectoryBehavior.CONTINUE;
        }
    }

}

    @Override
    protected void onFile(File file, String relPath) throws IOException {
        FileInstall task = new FileInstall();
        String hash = Files.hash(file, hf).toString();
        String hashedPath = hash.substring(0, 2) + "/" + hash.substring(2, 4) + "/" + hash;
        File destPath = new File(destDir, hashedPath);
        task.setHash(hash);
        task.setLocation(hashedPath);
        task.setTo(relPath);
        destPath.getParentFile().mkdirs();
        ClientFileCollector.log.info(String.format("Adding %s from %s...", relPath, file.getAbsolutePath()));
        Files.copy(file, destPath);
        manifest.getTasks().add(task);
    }

}
