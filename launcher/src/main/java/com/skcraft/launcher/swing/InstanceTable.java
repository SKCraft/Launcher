/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import javax.swing.table.TableModel;

public class InstanceTable extends DefaultTable {

    public InstanceTable() {
        super();
        setTableHeader(null);
    }

    @Override
    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        try {
            getColumnModel().getColumn(0).setMaxWidth(42);
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
}
