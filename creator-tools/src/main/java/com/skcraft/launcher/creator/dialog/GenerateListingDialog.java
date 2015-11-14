/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.dialog;

import com.jidesoft.swing.SearchableUtils;
import com.jidesoft.swing.TableSearchable;
import com.skcraft.launcher.creator.Creator;
import com.skcraft.launcher.creator.model.swing.ListingType;
import com.skcraft.launcher.creator.model.swing.ListingTypeComboBoxModel;
import com.skcraft.launcher.swing.DefaultTable;
import com.skcraft.launcher.swing.DirectoryField;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.swing.TableColumnAdjuster;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class GenerateListingDialog extends JDialog {

    @Getter private final DirectoryField destDirField = new DirectoryField();
    @Getter private final JComboBox<ListingType> listingTypeCombo = new JComboBox<>(new ListingTypeComboBoxModel());
    @Getter private final JTable manifestsTable = new DefaultTable();
    @Getter private final JLabel gameKeyWarning = new JLabel("Selected listing type won't support adding modpacks using 'game keys'.", SwingHelper.createIcon(Creator.class, "warning_icon.png"), SwingConstants.LEFT);

    @Getter private final JButton editManifestButton = new JButton("Modify...");

    @Getter private final JButton generateButton = new JButton("Generate");
    @Getter private final JButton cancelButton = new JButton("Cancel");

    @Getter private final TableColumnAdjuster manifestsTableAdjuster = new TableColumnAdjuster(manifestsTable);

    public GenerateListingDialog(Window parent) {
        super(parent, "Generate Package Listing", ModalityType.DOCUMENT_MODAL);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        manifestsTableAdjuster.adjustColumns();
        manifestsTableAdjuster.setDynamicAdjustment(true);

        manifestsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        manifestsTable.setAutoCreateRowSorter(true);

        JPanel container = new JPanel();
        container.setLayout(new MigLayout("insets dialog, fill", "[grow 0][grow 100]"));

        container.add(new JLabel("Output Directory:"));
        container.add(destDirField, "span");

        container.add(new JLabel("Package Listing Type:"));
        container.add(listingTypeCombo, "span");
        container.add(gameKeyWarning, "span, skip 1, hidemode 3");

        container.add(new JLabel("Modpacks to Include:"), "span, gaptop unrel");
        container.add(SwingHelper.wrapScrollPane(manifestsTable), "grow, pushy, span, w 500:650, h 170");
        container.add(editManifestButton, "gapbottom unrel, span, split 2");
        container.add(new JLabel("<html>Previously-selected modpacks and those in the _upload directory are the available options."), "gapbottom unrel");

        container.add(generateButton, "tag ok, span, split 2, sizegroup bttn");
        container.add(cancelButton, "tag cancel, sizegroup bttn");

        add(container, BorderLayout.CENTER);

        getRootPane().setDefaultButton(generateButton);
        getRootPane().registerKeyboardAction(e -> cancelButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        TableSearchable tableSearchable = SearchableUtils.installSearchable(manifestsTable);
        tableSearchable.setMainIndex(-1);
    }

}
