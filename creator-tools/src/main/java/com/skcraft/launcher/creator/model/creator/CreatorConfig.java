/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.model.creator;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

@Data
public class CreatorConfig {

    private List<RecentEntry> recentEntries = Lists.newArrayList();
    private boolean offlineEnabled;

    public void setRecentEntries(List<RecentEntry> recentEntries) {
        this.recentEntries = recentEntries != null ? recentEntries : Lists.newArrayList();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
