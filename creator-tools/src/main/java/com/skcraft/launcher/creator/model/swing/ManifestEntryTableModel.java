/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.model.swing;

import com.google.common.base.Joiner;
import com.skcraft.launcher.creator.model.creator.ManifestEntry;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ManifestEntryTableModel extends AbstractTableModel {

    private final Joiner GAME_KEY_JOINER = Joiner.on(", ");
    private final List<ManifestEntry> entries;

    public ManifestEntryTableModel(List<ManifestEntry> entries) {
        this.entries = entries;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "";
            case 1:
                return "Modpack";
            case 2:
                return "Version";
            case 3:
                return "Priority";
            case 4:
                return "Location";
            case 5:
                return "Game Keys";
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Boolean.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            case 3:
                return Integer.class;
            case 4:
                return String.class;
            case 5:
                return String.class;
            default:
                return null;
        }
    }
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                entries.get(rowIndex).setSelected((Boolean) value);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return true;
            default:
                return false;
        }
    }

    @Override
    public int getRowCount() {
        return entries.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ManifestEntry entry = entries.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return entry.isSelected();
            case 1:
                String title = entry.getManifestInfo().getTitle();
                if (title != null) {
                    return title;
                } else {
                    return entry.getManifestInfo().getName();
                }
            case 2:
                return entry.getManifestInfo().getVersion();
            case 3:
                return entry.getManifestInfo().getPriority();
            case 4:
                return entry.getManifestInfo().getLocation();
            case 5:
                List<String> gameKeys = entry.getGameKeys();
                return gameKeys != null ? GAME_KEY_JOINER.join(gameKeys) : "";
            default:
                return null;
        }
    }

}
