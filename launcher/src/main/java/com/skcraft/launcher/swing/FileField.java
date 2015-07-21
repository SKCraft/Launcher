/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class FileField extends DirectoryField {

    private final String title;
    private final String description;

    public FileField(String title, String description) {
        this.title = title;
        this.description = description;
    }

    @Override
    protected JFileChooser getFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return true;
            }

            @Override
            public String getDescription() {
                return description;
            }
        });

        return chooser;
    }

}
