/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.model.creator;

import lombok.Data;

import java.io.File;

@Data
public class RecentEntry {

    private File path;

    public void setPath(File path) {
        this.path = path != null ? path : new File(".");
    }

    @Override
    public String toString() {
        return path.getAbsolutePath();
    }

}
