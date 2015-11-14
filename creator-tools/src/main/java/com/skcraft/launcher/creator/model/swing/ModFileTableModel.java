/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.model.swing;

import com.skcraft.launcher.creator.Creator;
import com.skcraft.launcher.creator.model.creator.ModFile;
import com.skcraft.launcher.swing.SwingHelper;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ModFileTableModel extends AbstractTableModel {

    private static final Icon WWW_ICON;
    private final List<ModFile> mods;

    static {
        WWW_ICON = SwingHelper.createIcon(Creator.class, "www_icon.png");
    }

    public ModFileTableModel(List<ModFile> mods) {
        checkNotNull(mods, "mods");
        this.mods = mods;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0: return "";
            case 1: return "Mod";
            case 2: return "Version";
            case 3: return "Latest Release";
            case 4: return "Latest Dev";
            case 5: return "Mod ID";
            case 6: return "Filename";
            default: return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Icon.class;
            default:
                return String.class;
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public int getRowCount() {
        return mods.size();
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        @Nullable ModFile mod = mods.get(rowIndex);

        if (mod == null) {
            return null;
        }

        switch (columnIndex) {
            case 0: return mod.getUrl() != null ? WWW_ICON : null;
            case 1: return mod.getName() != null ? mod.getName() : mod.getFile().getName();
            case 2: return mod.getCleanVersion();
            case 3: return mod.getLatestVersion();
            case 4: return mod.getLatestDevVersion();
            case 5: return mod.getModId();
            case 6: return mod.getFile().getName();
            default:  return null;
        }
    }

    public ModFile getMod(int index) {
        return mods.get(index);
    }

}
