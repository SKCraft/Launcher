/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Utility method to make {@link ActionListeners}.
 */
public final class ActionListeners {

    private ActionListeners() {
    }

    public static ActionListener dispose(final Window window) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.dispose();
            }
        };
    }

    public static ActionListener openURL(final Component component, final String url) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingHelper.openURL(url, component);
            }
        };
    }

    public static ActionListener browseDir(
            final Component component, final File dir, final boolean create) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (create) {
                    dir.mkdirs();
                }
                SwingHelper.browseDir(dir, component);
            }
        };
    }

}
