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

    private final JButton editConfigButton = new JButton("Edit modpack.json", SwingHelper.readImageIcon(ToolsFrame.class, "edit.png"));
    private final JButton openFolderButton = new JButton("Open Modpack Files", SwingHelper.readImageIcon(ToolsFrame.class, "open_folder.png"));
    private final JButton checkProblemsButton = new JButton("Check for Potential Problems", SwingHelper.readImageIcon(ToolsFrame.class, "check.png"));
    private final JButton testButton = new JButton("Run Modpack", SwingHelper.readImageIcon(ToolsFrame.class, "test.png"));
    private final JButton buildButton = new JButton("Build Client Modpack", SwingHelper.readImageIcon(ToolsFrame.class, "build.png"));
    private final JButton deployServerButton = new JButton("Copy Server Mods to Folder", SwingHelper.readImageIcon(ToolsFrame.class, "server.png"));
    private final JButton optionsButton = new JButton("Test Launcher Options", SwingHelper.readImageIcon(ToolsFrame.class, "options.png"));
    private final JButton clearInstanceButton = new JButton("Delete Instance from Test Launcher", SwingHelper.readImageIcon(ToolsFrame.class, "clean.png"));
    private final JButton clearWebRootButton = new JButton("Delete Generated Modpack Files", SwingHelper.readImageIcon(ToolsFrame.class, "clean.png"));
    private final JButton openConsoleButton = new JButton("Console");
    private final JButton docsButton = new JButton("Help");
    private final JButton quitButton = new JButton("Quit");

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
        container.setLayout(new MigLayout("fill, insets dialog, wrap 1"));

        BufferedImage header = SwingHelper.readIconImage(ToolsFrame.class, "header.png");
        if (header != null) {
            add(new JLabel(new ImageIcon(header)), BorderLayout.NORTH);
        }

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Create", null, createProjectPanel());
        tabbedPane.addTab("Test", null, createTestPanel());
        tabbedPane.addTab("Release", null, createReleasePanel());
        container.add(tabbedPane, "grow, gapbottom 20");

        container.add(docsButton, "span, split 3, sizegroup bttn");
        container.add(openConsoleButton, "sizegroup bttn");
        container.add(quitButton, "tag ok, sizegroup bttn");

        add(container, BorderLayout.CENTER);
    }

    private JPanel createProjectPanel() {
        JPanel container = new JPanel();
        SwingHelper.removeOpaqueness(container);
        container.setLayout(new MigLayout("fillx, insets dialog, wrap 1", "", ""));

        container.add(editConfigButton, "grow, tag ok");
        container.add(openFolderButton, "grow, tag ok");
        container.add(checkProblemsButton, "grow, tag ok");

        return container;
    }

    private JPanel createTestPanel() {
        JPanel container = new JPanel();
        SwingHelper.removeOpaqueness(container);
        container.setLayout(new MigLayout("fillx, insets dialog, wrap 1", "", ""));

        container.add(testButton, "grow, tag ok");
        container.add(optionsButton, "grow, tag ok");
        container.add(clearInstanceButton, "grow, tag ok");
        container.add(clearWebRootButton, "grow, tag ok");

        return container;
    }

    private JPanel createReleasePanel() {
        JPanel container = new JPanel();
        SwingHelper.removeOpaqueness(container);
        container.setLayout(new MigLayout("fillx, insets dialog, wrap 1", "", ""));

        container.add(buildButton, "grow, tag ok");
        container.add(deployServerButton, "grow, tag ok");

        return container;
    }

}
