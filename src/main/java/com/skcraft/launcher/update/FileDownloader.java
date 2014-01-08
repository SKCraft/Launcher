/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.update;

import lombok.Getter;
import lombok.ToString;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

@ToString(exclude = "installer")
public class FileDownloader implements Runnable {

    private final Installer installer;
    @Getter private final URL url;
    @Getter private final List<File> targets;

    public FileDownloader(Installer installer, URL url, List<File> targets) {
        this.installer = installer;
        this.url = url;
        this.targets = targets;
    }

    @Override
    public void run() {
        try {
            File sourceFile = installer.download(url, "");
            installer.submit(new FileDistribute(sourceFile, targets));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new RuntimeException("Failed to download " + url, e);
        }
    }

}
