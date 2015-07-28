/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.dialog;

import com.jidesoft.swing.SearchableUtils;
import com.jidesoft.swing.TableSearchable;
import com.skcraft.launcher.swing.DefaultTable;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.swing.TableColumnAdjuster;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class VersionCheckDialog extends JDialog {

    @Getter private final JTable knownModsTable = new DefaultTable();
    @Getter private final JTable unknownModsTable = new DefaultTable();
    @Getter private final JButton closeButton = new JButton("Close");
    private final TableColumnAdjuster updateTableAdjuster = new TableColumnAdjuster(knownModsTable);
    private final TableColumnAdjuster unknownTableAdjuster = new TableColumnAdjuster(unknownModsTable);

    public VersionCheckDialog(Window parent) {
        super(parent, "Update Check", ModalityType.DOCUMENT_MODAL);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        updateTableAdjuster.adjustColumns();
        updateTableAdjuster.setDynamicAdjustment(true);

        unknownTableAdjuster.adjustColumns();
        unknownTableAdjuster.setDynamicAdjustment(true);

        knownModsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        knownModsTable.setAutoCreateRowSorter(true);

        unknownModsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        unknownModsTable.setAutoCreateRowSorter(true);

        JPanel container = new JPanel();
        container.setLayout(new MigLayout("insets dialog, fill"));

        container.add(new JLabel("With Potential Updates:"), "span");
        container.add(SwingHelper.wrapScrollPane(knownModsTable), "grow, pushy, span, w 500:900, h 230");

        container.add(new JLabel("Unknown Status:"), "span");
        container.add(SwingHelper.wrapScrollPane(unknownModsTable), "grow, pushy, span, w 500:900, h 150, gapbottom unrel, wrap");

        container.add(new JLabel("Version data is sourced from NotEnoughMods.com."), "");
        container.add(closeButton, "tag cancel, sizegroup bttn");

        add(container, BorderLayout.CENTER);

        getRootPane().registerKeyboardAction(e -> closeButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        TableSearchable tableSearchable;

        tableSearchable = SearchableUtils.installSearchable(knownModsTable);
        tableSearchable.setMainIndex(-1);

        tableSearchable = SearchableUtils.installSearchable(unknownModsTable);
        tableSearchable.setMainIndex(-1);
    }

}
