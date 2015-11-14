/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.dialog;

import com.skcraft.launcher.creator.Creator;
import com.skcraft.launcher.creator.model.creator.RecentEntry;
import com.skcraft.launcher.creator.swing.BorderCellRenderer;
import com.skcraft.launcher.swing.SwingHelper;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class WelcomeDialog extends JFrame {

    @Getter private final JButton newButton = new JButton("New Workspace...", SwingHelper.createIcon(Creator.class, "new.png"));
    @Getter private final JButton openButton = new JButton("Open Workspace...", SwingHelper.createIcon(Creator.class, "open_folder.png"));
    @Getter private final JButton helpButton = new JButton("Help");
    @Getter private final JButton aboutButton = new JButton("About");
    @Getter private final JButton quitButton = new JButton("Quit");
    @Getter private final JList<RecentEntry> recentList = new JList<>();

    public WelcomeDialog() {
        super("Modpack Creator");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initComponents();
        setResizable(false);
        pack();
        setLocationRelativeTo(null);

        SwingHelper.setFrameIcon(this, Creator.class, "icon.png");
    }

    private void initComponents() {
        recentList.setCellRenderer(new BorderCellRenderer(BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JPanel container = new JPanel();
        container.setLayout(new MigLayout("insets 50 20"));

        container.add(new JLabel(SwingHelper.createIcon(Creator.class, "welcome_logo.png")), "wrap, gap 20 20, gapbottom 30");

        container.add(newButton, "grow, gap 50 50, wrap");
        container.add(openButton, "grow, gap 50 50, wrap");

        JScrollPane recentScrollPane = new JScrollPane(recentList);
        recentScrollPane.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY));
        recentScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        container.add(recentScrollPane, "dock east, w 280, h 390");

        JPanel buttons = new JPanel();
        buttons.setLayout(new MigLayout("insets 20", "[][]push[]"));
        buttons.add(helpButton);
        buttons.add(aboutButton);
        buttons.add(quitButton);
        container.add(buttons, "dock south");

        add(container, BorderLayout.CENTER);

        getRootPane().registerKeyboardAction(e -> quitButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }


}
