/*
 * SKCraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class FancyBackgroundPanel extends JPanel {

    private Image background;

    public FancyBackgroundPanel() {
        try {
            background = ImageIO.read(FancyBackgroundPanel.class.getResourceAsStream("launcher_bg.jpg"));
        } catch (IOException e) {
            background = null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, null);
        }
    }

}
