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
            double multi;
            int w, h;

            // Calculate Aspect Ratio Multiplier depending on window size
            if (this.getHeight() <= this.getWidth()) {
                multi = this.getWidth() / (float)background.getWidth(null);
            }
            else {
                multi = this.getHeight() / (float)background.getHeight(null);
            }

            // Calculate new width and height
            w = (int) Math.floor((float)background.getWidth(null) * multi);
            h = (int) Math.floor((float)background.getHeight(null) * multi);

            // Check if it needs to be switched (eg. in case of a square window)
            if (h < this.getHeight() || w < this.getWidth()) {
                if (h < this.getHeight()) {
                    multi = this.getHeight() / (float)background.getHeight(null);
                }
                else if (w < this.getWidth()) {
                    multi = this.getWidth() / (float) background.getWidth(null);
                }

                w = (int) Math.floor((float)background.getWidth(null) * multi);
                h = (int) Math.floor((float)background.getHeight(null) * multi);
            }

            g.drawImage(background, 0, 0, w, h,null);
        }
    }
}
