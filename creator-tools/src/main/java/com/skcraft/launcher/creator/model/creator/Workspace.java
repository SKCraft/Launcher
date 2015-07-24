/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.model.creator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.skcraft.launcher.creator.model.swing.ListingType;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Data
public class Workspace {

    public static final String DIR_NAME = ".modpacks";
    public static final String FILENAME = "workspace.json";

    @JsonIgnore private File directory;
    private List<Pack> packs = Lists.newArrayList();
    private List<ManifestEntry> packageListingEntries = Lists.newArrayList();
    private ListingType packageListingType = ListingType.STATIC;

    public void setPacks(List<Pack> packs) {
        this.packs = packs != null ? packs : Lists.<Pack>newArrayList();
    }

    public void setPackageListingEntries(List<ManifestEntry> entries) {
        this.packageListingEntries = entries != null ? entries : Lists.newArrayList();
    }

    public void setPackageListingType(ListingType packageListingType) {
        this.packageListingType = packageListingType != null ? packageListingType : ListingType.STATIC;
    }

    public boolean hasPack(File dir) {
        for (Pack pack : packs) {
            try {
                if (pack.getDirectory().getCanonicalPath().equals(dir.getCanonicalPath())) {
                    return true;
                }
            } catch (IOException ignored) {
            }
        }

        return false;
    }

    public void load() {
        for (Pack pack : getPacks()) {
            pack.setWorkspace(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public static File getDataDir(File workspaceDir) {
        return new File(workspaceDir, DIR_NAME);
    }

    public static File getWorkspaceFile(File workspaceDir) {
        return new File(getDataDir(workspaceDir), FILENAME);
    }

}
