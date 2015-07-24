/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.model.swing;

import javax.swing.*;

public class ListingTypeComboBoxModel extends AbstractListModel<ListingType> implements ComboBoxModel<ListingType> {

    private ListingType selection = ListingType.STATIC;

    @Override
    public void setSelectedItem(Object anItem) {
        selection = (ListingType) anItem;
    }

    @Override
    public ListingType getSelectedItem() {
        return selection;
    }

    @Override
    public int getSize() {
        return ListingType.values().length;
    }

    @Override
    public ListingType getElementAt(int index) {
        return ListingType.values()[index];
    }

}
