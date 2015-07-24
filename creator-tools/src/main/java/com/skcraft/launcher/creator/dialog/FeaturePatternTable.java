/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.dialog;

import com.skcraft.launcher.swing.DefaultTable;

import javax.swing.table.TableModel;

class FeaturePatternTable extends DefaultTable {

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
