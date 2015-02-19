/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * An implementation of MouseAdapter that makes it easier to handle right click menus.
 */
public abstract class PopupMouseAdapter extends MouseAdapter {

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    protected abstract void showPopup(MouseEvent e);

}
