/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */
package com.skcraft.launcher.swing;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import javax.swing.table.DefaultTableCellRenderer;

public class InstanceTable extends JTable {
    
    public static InstanceTable instanceTable;

    public InstanceTable() {
        instanceTable = this;
        setShowGrid(true);
        setGridColor(new Color(80, 80, 80));
        setShowVerticalLines(false);
        setRowHeight(Math.max(getRowHeight() + 100, 50));
        setIntercellSpacing(new Dimension(0, 0));
        setFillsViewportHeight(true);
        setTableHeader(null);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setVerticalAlignment(JLabel.TOP);
        setDefaultRenderer(String.class, centerRenderer);
    }

    @Override
    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        try {
            getColumnModel().getColumn(0).setMaxWidth(128);
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
}
