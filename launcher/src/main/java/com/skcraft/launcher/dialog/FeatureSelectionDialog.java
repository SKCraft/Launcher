/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.dialog;

import com.skcraft.launcher.model.modpack.Feature;
import com.skcraft.launcher.swing.*;
import com.skcraft.launcher.util.SharedLocale;
import lombok.NonNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;

import static javax.swing.BorderFactory.createEmptyBorder;

public class FeatureSelectionDialog extends JDialog {

    private final List<Feature> features;
    private final JPanel container = new JPanel(new BorderLayout());
    private final JTextArea descText = new JTextArea(SharedLocale.tr("features.selectForInfo"));
    private final JScrollPane descScroll = new JScrollPane(descText);
    private final CheckboxTable componentsTable = new CheckboxTable();
    private final JScrollPane componentsScroll = new JScrollPane(componentsTable);
    private final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, componentsScroll, descScroll);
    private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);
    private final JButton installButton = new JButton(SharedLocale.tr("features.install"));

    public FeatureSelectionDialog(Window owner, @NonNull List<Feature> features) {
        super(owner, ModalityType.DOCUMENT_MODAL);

        this.features = features;

        setTitle(SharedLocale.tr("features.title"));
        initComponents();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(500, 400));
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        componentsTable.setModel(new FeatureTableModel(features));

        descScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        descText.setFont(new JLabel().getFont());
        descText.setEditable(false);
        descText.setWrapStyleWord(true);
        descText.setLineWrap(true);
        SwingHelper.removeOpaqueness(descText);
        descText.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);

        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(6);
        SwingHelper.flattenJSplitPane(splitPane);

        container.setBorder(createEmptyBorder(12, 12, 12, 12));
        container.add(splitPane, BorderLayout.CENTER);

        buttonsPanel.addGlue();
        buttonsPanel.addElement(installButton);

        JLabel descLabel = new JLabel(SharedLocale.tr("features.intro"));
        descLabel.setBorder(createEmptyBorder(12, 12, 4, 12));

        SwingHelper.equalWidth(installButton, new JButton(SharedLocale.tr("button.cancel")));

        add(descLabel, BorderLayout.NORTH);
        add(container, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        componentsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                updateDescription();
            }
        });

        installButton.addActionListener(ActionListeners.dispose(this));
    }

    private void updateDescription() {
        Feature feature = features.get(componentsTable.getSelectedRow());

        if (feature != null) {
            descText.setText(feature.getDescription());
        } else {
            descText.setText(SharedLocale.tr("features.selectForInfo"));
        }
    }

}
