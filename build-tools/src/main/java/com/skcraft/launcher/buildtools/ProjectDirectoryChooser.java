/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.buildtools;

import com.skcraft.launcher.builder.BuilderOptions;
import lombok.Getter;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

class ProjectDirectoryChooser {

    @Getter
    private File inputDir;

    public ProjectDirectoryChooser(File inputDir) {
        this.inputDir = inputDir;
    }

    public File choose() throws InvocationTargetException, InterruptedException {
        final File currentDir = new File(".");

        if (inputDir == null) {
            if (new File(currentDir, BuilderOptions.DEFAULT_CONFIG_FILENAME).exists()) {
                inputDir = currentDir;
            } else {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        inputDir = ProjectDirectoryDialog.showDirectoryDialog(null, currentDir);
                    }
                });
            }
        }

        return inputDir;
    }

}
