/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import javax.swing.*;
import java.awt.*;

public class HeaderPanel extends JPanel {

    public HeaderPanel() {
        setBackground(new Color(0xDB5036));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 60);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }
}
