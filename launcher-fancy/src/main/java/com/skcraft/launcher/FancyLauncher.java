/*
 * SKCraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import javax.swing.*;

public class FancyLauncher {

    public static void main(String[] args) {
        UIManager.getLookAndFeelDefaults().put("ClassLoader", FancyLauncher.class.getClassLoader());
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        System.setProperty("sun.awt.noerasebackground", "true");
        System.setProperty("substancelaf.windowRoundedCorners", "false");
        System.setProperty("com.skcraft.launcher.laf", "org.pushingpixels.substance.api.skin.SubstanceMarinerLookAndFeel");
        Launcher.main(args);
    }

}
