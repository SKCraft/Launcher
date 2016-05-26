/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import javax.swing.*;
import java.awt.*;

/**
 * The default table style used throughout the launcher.
 */
public class DefaultTable extends JTable {

    public DefaultTable() {
        setShowGrid(false);
        setRowHeight((int) (Math.max(getRowHeight(), new JCheckBox().getPreferredSize().getHeight() - 2)));
        setIntercellSpacing(new Dimension(0, 0));
        setFillsViewportHeight(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

}
