/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.buildtools.compile;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;

class ProblemTable extends JTable {

    public ProblemTable() {
        setShowGrid(false);
        setRowHeight((int) (Math.max(getRowHeight(), new JCheckBox().getPreferredSize().getHeight() - 2)));
        setIntercellSpacing(new Dimension(0, 0));
        setFillsViewportHeight(true);
        setTableHeader(null);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    @Override
    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        try {
            getColumnModel().getColumn(0).setMaxWidth(20);
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
    }


}
