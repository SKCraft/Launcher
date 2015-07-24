/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.swing;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class WorkspaceDirectoryFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        return f.isDirectory();
    }

    @Override
    public String getDescription() {
        return "Directories";
    }

}
