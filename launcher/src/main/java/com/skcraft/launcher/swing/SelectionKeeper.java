/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import lombok.NonNull;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class SelectionKeeper implements ListSelectionListener, ListDataListener {

    private final JList list;
    private Object lastSelected;

    private SelectionKeeper(@NonNull JList list) {
        this.list = list;
    }

    public void intervalAdded(ListDataEvent e) {
        list.setSelectedValue(lastSelected, true);
    }

    public void intervalRemoved(ListDataEvent e) {
        list.setSelectedValue(lastSelected, true);
    }

    public void contentsChanged(ListDataEvent e) {
        list.setSelectedValue(lastSelected, true);
    }

    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            lastSelected = list.getSelectedValue();
        }
    }

    public static void attach(@NonNull JList list) {
        SelectionKeeper s = new SelectionKeeper(list);
        list.addListSelectionListener(s);
        list.getModel().addListDataListener(s);
    }

}
