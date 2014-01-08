/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import javax.swing.*;
import java.awt.*;

public class WebpageLayoutManager implements LayoutManager {

    private static final int PROGRESS_WIDTH = 100;
    
    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        throw new UnsupportedOperationException("Can't remove things!");
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return new Dimension(0, 0);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(0, 0);
    }

    @Override
    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        int maxWidth = parent.getWidth() - (insets.left + insets.right);
        int maxHeight = parent.getHeight() - (insets.top + insets.bottom);
        
        int numComps = parent.getComponentCount();
        for (int i = 0 ; i < numComps ; i++) {
            Component comp = parent.getComponent(i);
            
            if (comp instanceof JProgressBar) {
                Dimension size = comp.getPreferredSize();
                comp.setLocation((parent.getWidth() - PROGRESS_WIDTH) / 2,
                        (int) (parent.getHeight() / 2.0 - size.height / 2.0));
                comp.setSize(PROGRESS_WIDTH,
                        (int) comp.getPreferredSize().height);
            } else {
                comp.setLocation(insets.left, insets.top);
                comp.setSize(maxWidth, maxHeight);
            }
        }
    }

}
