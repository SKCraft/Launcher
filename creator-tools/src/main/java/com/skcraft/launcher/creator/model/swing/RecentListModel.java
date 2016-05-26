/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.model.swing;

import com.skcraft.launcher.creator.model.creator.RecentEntry;

import javax.swing.*;
import java.util.List;

public class RecentListModel extends AbstractListModel<RecentEntry> {

    private final List<RecentEntry> recentEntries;

    public RecentListModel(List<RecentEntry> recentEntries) {
        this.recentEntries = recentEntries;
    }

    @Override
    public int getSize() {
        return recentEntries.size();
    }

    @Override
    public RecentEntry getElementAt(int index) {
        return recentEntries.get(index);
    }

    public void fireUpdate() {
        fireContentsChanged(this, 0, Integer.MAX_VALUE);
    }
}
