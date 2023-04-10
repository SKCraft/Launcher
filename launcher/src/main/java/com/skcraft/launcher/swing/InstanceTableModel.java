/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import com.skcraft.launcher.*;
import com.skcraft.launcher.instance.*;
import com.skcraft.launcher.util.SharedLocale;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

public class InstanceTableModel extends AbstractTableModel {

    private final InstanceList instances;
    private final Icon instanceIcon;
    private final Icon instanceCollectionIcon;
    private final Icon customInstanceIcon;
    private final Icon downloadIcon;
    private final Icon newsIcon;

    public InstanceTableModel(InstanceList instances) {
        this.instances = instances;
        instanceIcon = SwingHelper.createIcon(Launcher.class, "instance_icon.png", 16, 16);
        instanceCollectionIcon = SwingHelper.createIcon(Launcher.class, "collection_icon.png", 16, 16);
        customInstanceIcon = SwingHelper.createIcon(Launcher.class, "custom_instance_icon.png", 16, 16);
        downloadIcon = SwingHelper.createIcon(Launcher.class, "download_icon.png", 16, 16);
        newsIcon = SwingHelper.createIcon(Launcher.class, "news_icon.png", 16, 16);
    }

    public void update() {
        instances.sort();
        fireTableDataChanged();
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "";
            case 1:
                return SharedLocale.tr("launcher.modpackColumn");
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return ImageIcon.class;
            case 1:
                return String.class;
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                instances.get(rowIndex).setSelected((boolean) (Boolean) value);
                break;
            case 1:
            default:
                break;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return false;
            case 1:
                return false;
            default:
                return false;
        }
    }

    @Override
    public int getRowCount() {
        return instances.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Instance instance;
        switch (columnIndex) {
            case 0:
                instance = instances.get(rowIndex);
                if (instance instanceof InstanceSpacer) {
                    return null;
                } else if (instance instanceof InstanceNews) {
                    return newsIcon;
                } else if (instance instanceof InstanceCollection) {
                    return instanceCollectionIcon;
                } else if (!instance.isLocal()) {
                    return downloadIcon;
                } else if (instance.getManifestURL() != null) {
                    return instanceIcon;
                } else {
                    return customInstanceIcon;
                }
            case 1:
                instance = instances.get(rowIndex);
                if (instance.isInCollection()) {
                    return instance.getName().replace(instance.getTitle(), "");
                } else {
                    return instance.getTitle();
                }
            default:
                return null;
        }
    }

}
