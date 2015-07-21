/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.buildtools.project;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;

class FeaturePatternTable extends JTable {

    public FeaturePatternTable() {
        setShowGrid(false);
        setRowHeight((int) (Math.max(getRowHeight(), new JCheckBox().getPreferredSize().getHeight() - 2)));
        setIntercellSpacing(new Dimension(0, 0));
        setFillsViewportHeight(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    @Override
    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        try {
            getColumnModel().getColumn(1).setMaxWidth(80);
            getColumnModel().getColumn(2).setMaxWidth(80);
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
    }
}
