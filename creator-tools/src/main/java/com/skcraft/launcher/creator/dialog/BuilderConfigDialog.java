/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.dialog;

import com.google.common.base.Strings;
import com.jidesoft.swing.SearchableUtils;
import com.jidesoft.swing.TableSearchable;
import com.skcraft.launcher.builder.BuilderConfig;
import com.skcraft.launcher.builder.FeaturePattern;
import com.skcraft.launcher.builder.FnPatternList;
import com.skcraft.launcher.creator.model.swing.FeaturePatternTableModel;
import com.skcraft.launcher.model.modpack.LaunchModifier;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.swing.TextFieldPopupMenu;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class BuilderConfigDialog extends JDialog {

    private final JTextField nameText = new JTextField(20);
    private final JTextField titleText = new JTextField(30);
    private final JTextField gameVersionText = new JTextField(10);
    private final JTextArea launchFlagsArea = new JTextArea(10, 40);
    private final JTextArea userFilesIncludeArea = new JTextArea(15, 40);
    private final JTextArea userFilesExcludeArea = new JTextArea(8, 40);
    private final FeaturePatternTable featuresTable = new FeaturePatternTable();
    private FeaturePatternTableModel featuresModel;

    private final BuilderConfig config;
    private boolean saved = false;

    public BuilderConfigDialog(Window parent, BuilderConfig config) {
        super(parent, "Modpack Properties", ModalityType.DOCUMENT_MODAL);

        this.config = config;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initComponents();
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);

        copyFrom();

        nameText.requestFocus();
    }

    private void initComponents() {
        nameText.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);
        titleText.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);
        gameVersionText.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);
        launchFlagsArea.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);
        userFilesIncludeArea.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);

        launchFlagsArea.setFont(nameText.getFont());
        userFilesIncludeArea.setFont(nameText.getFont());
        userFilesExcludeArea.setFont(nameText.getFont());

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel container = new JPanel();
        container.setLayout(new MigLayout("fill, insets dialog"));

        tabbedPane.addTab("Modpack", null, createMainPanel());
        tabbedPane.addTab("Launch", null, createLaunchPanel());
        tabbedPane.addTab("User Files", null, createUserFilesPanel());
        tabbedPane.addTab("Optional Features", null, createFeaturesPanel());

        container.add(tabbedPane, "span, grow, gapbottom unrel");

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        container.add(saveButton, "tag ok, span, split 2, sizegroup bttn");
        container.add(cancelButton, "tag cancel, sizegroup bttn");

        getRootPane().setDefaultButton(saveButton);
        getRootPane().registerKeyboardAction(event -> cancelButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        add(container, BorderLayout.CENTER);

        saveButton.addActionListener(e -> {
            if (nameText.getText().trim().isEmpty()) {
                SwingHelper.showErrorDialog(BuilderConfigDialog.this, "The 'Name' field cannot be empty.", "Input Error");
                return;
            }

            if (gameVersionText.getText().trim().isEmpty()) {
                SwingHelper.showErrorDialog(BuilderConfigDialog.this, "The 'Game Version' field must be a Minecraft version.", "Input Error");
                return;
            }

            copyTo();
            saved = true;
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());

        TableSearchable tableSearchable = SearchableUtils.installSearchable(featuresTable);
        tableSearchable.setMainIndex(-1);
    }

    private JPanel createMainPanel() {
        JPanel container = new JPanel();
        SwingHelper.removeOpaqueness(container);
        container.setLayout(new MigLayout("insets dialog"));

        container.add(new JLabel("Name:"));
        container.add(nameText, "span");

        container.add(new JLabel("Title:"));
        container.add(titleText, "span");

        container.add(new JLabel("Game Version:"));
        container.add(gameVersionText, "span");

        return container;
    }

    private JPanel createLaunchPanel() {
        JPanel container = new JPanel();
        SwingHelper.removeOpaqueness(container);
        container.setLayout(new MigLayout("insets dialog"));

        container.add(new JLabel("Launch Flags:"), "wrap");
        container.add(SwingHelper.wrapScrollPane(launchFlagsArea), "span");

        return container;
    }

    private JPanel createUserFilesPanel() {
        JPanel container = new JPanel();
        SwingHelper.removeOpaqueness(container);
        container.setLayout(new MigLayout("insets dialog"));

        container.add(new JLabel("Include Patterns:"), "wrap");
        container.add(SwingHelper.wrapScrollPane(userFilesIncludeArea), "span, gapbottom unrel");

        container.add(new JLabel("Exclude Patterns:"), "wrap");
        container.add(SwingHelper.wrapScrollPane(userFilesExcludeArea), "span");

        return container;
    }

    private JPanel createFeaturesPanel() {
        JPanel container = new JPanel();
        SwingHelper.removeOpaqueness(container);
        container.setLayout(new MigLayout("fill, insets dialog"));

        JButton newButton = new JButton("New...");
        JButton editButton = new JButton("Edit...");
        JButton deleteButton = new JButton("Delete...");

        container.add(newButton, "span, split 3, sizegroup bttn");
        container.add(editButton, "sizegroup bttn");
        container.add(deleteButton, "sizegroup bttn");

        container.add(SwingHelper.wrapScrollPane(featuresTable), "grow, w 10:100:null, gaptop 10");

        newButton.addActionListener(e -> {
            FeaturePattern pattern = new FeaturePattern();
            if (FeaturePatternDialog.showEditor(BuilderConfigDialog.this, pattern)) {
                featuresModel.addFeature(pattern);
            }
        });

        editButton.addActionListener(e -> {
            int index = featuresTable.getSelectedRow();
            if (index > -1) {
                FeaturePattern pattern = featuresModel.getFeature(index);
                FeaturePatternDialog.showEditor(BuilderConfigDialog.this, pattern);
                featuresModel.fireTableDataChanged();
            } else {
                SwingHelper.showErrorDialog(BuilderConfigDialog.this, "Select a feature first.", "No Selection");
            }
        });

        deleteButton.addActionListener(e -> {
            int index = featuresTable.getSelectedRow();
            if (index > -1) {
                FeaturePattern pattern = featuresModel.getFeature(index);
                if (SwingHelper.confirmDialog(BuilderConfigDialog.this, "Are you sure that you want to delete '" + pattern.getFeature().getName() + "'?", "Delete")) {
                    featuresModel.removeFeature(index);
                }
            } else {
                SwingHelper.showErrorDialog(BuilderConfigDialog.this, "Select a feature first.", "No Selection");
            }
        });

        return container;
    }

    private void copyFrom() {
        SwingHelper.setTextAndResetCaret(nameText, config.getName());
        SwingHelper.setTextAndResetCaret(titleText, config.getTitle());
        SwingHelper.setTextAndResetCaret(gameVersionText, config.getGameVersion());
        SwingHelper.setTextAndResetCaret(launchFlagsArea, SwingHelper.listToLines(config.getLaunchModifier().getFlags()));
        SwingHelper.setTextAndResetCaret(userFilesIncludeArea, SwingHelper.listToLines(config.getUserFiles().getInclude()));
        SwingHelper.setTextAndResetCaret(userFilesExcludeArea, SwingHelper.listToLines(config.getUserFiles().getExclude()));
        featuresModel = new FeaturePatternTableModel(config.getFeatures());
        featuresTable.setModel(featuresModel);
    }

    private void copyTo() {
        config.setName(nameText.getText().trim());
        config.setTitle(Strings.emptyToNull(titleText.getText().trim()));
        config.setGameVersion(gameVersionText.getText().trim());

        LaunchModifier launchModifier = config.getLaunchModifier();
        FnPatternList userFiles = config.getUserFiles();

        launchModifier.setFlags(SwingHelper.linesToList(launchFlagsArea.getText()));
        userFiles.setInclude(SwingHelper.linesToList(userFilesIncludeArea.getText()));
        userFiles.setExclude(SwingHelper.linesToList(userFilesExcludeArea.getText()));
    }

    public static boolean showEditor(Window window, BuilderConfig config) {
        BuilderConfigDialog dialog = new BuilderConfigDialog(window, config);
        dialog.setVisible(true);
        return dialog.saved;
    }

}
