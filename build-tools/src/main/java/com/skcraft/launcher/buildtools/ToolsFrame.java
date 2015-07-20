/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.buildtools;

import com.skcraft.launcher.swing.SwingHelper;
import lombok.Data;
import lombok.extern.java.Log;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

@Log
@Data
public class ToolsFrame extends JFrame {

    private final JButton buildButton = new JButton("Build for Release", SwingHelper.readImageIcon(ToolsFrame.class, "build.png"));
    private final JButton testButton = new JButton("Run Modpack", SwingHelper.readImageIcon(ToolsFrame.class, "test.png"));
    private final JButton optionsButton = new JButton("Test Launcher Options", SwingHelper.readImageIcon(ToolsFrame.class, "options.png"));
    private final JButton clearInstanceButton = new JButton("Delete Instance from Test Launcher", SwingHelper.readImageIcon(ToolsFrame.class, "clean.png"));
    private final JButton clearWebRootButton = new JButton("Delete Generated Modpack Files", SwingHelper.readImageIcon(ToolsFrame.class, "clean.png"));
    private final JButton openConsoleButton = new JButton("Re-open Console", SwingHelper.readImageIcon(ToolsFrame.class, "log.png"));
    private final JButton docsButton = new JButton("View Documentation", SwingHelper.readImageIcon(ToolsFrame.class, "help.png"));

    public ToolsFrame() {
        super("Modpack Build Tools");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        initComponents();
        pack();
        setLocationRelativeTo(null);

        SwingHelper.setIconImage(this, ToolsFrame.class, "icon.png");
    }

    private void initComponents() {
        JPanel container = new JPanel();
        container.setLayout(new MigLayout("fill, insets dialog, wrap 1", "", ""));

        BufferedImage header = SwingHelper.readIconImage(ToolsFrame.class, "header.png");
        if (header != null) {
            add(new JLabel(new ImageIcon(header)), BorderLayout.NORTH);
        }

        container.add(buildButton, "grow, tag ok");
        container.add(testButton, "grow, tag ok");
        container.add(optionsButton, "grow, tag ok");
        container.add(clearInstanceButton, "grow, tag ok");
        container.add(clearWebRootButton, "grow, tag ok");
        container.add(openConsoleButton, "grow, tag ok");
        container.add(docsButton, "grow, tag ok");

        add(container, BorderLayout.CENTER);
    }

}
