/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import com.skcraft.launcher.Launcher;

import javax.swing.table.TableModel;

public class InstanceTable extends DefaultTable {

    public int lastSelected = getSelectedRow();
    public boolean switched = false;

    public InstanceTable() {
        super();
        setTableHeader(null);
        getSelectionModel().addListSelectionListener(e -> {
                int selected = getSelectedRow();
                if (lastSelected != selected) {
                    lastSelected = selected;
                    switched = true;
                } else {
                    switched = false;
                }
            }
        );
    }

    @Override
    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        try {
            getColumnModel().getColumn(0).setMaxWidth(24);
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

}
