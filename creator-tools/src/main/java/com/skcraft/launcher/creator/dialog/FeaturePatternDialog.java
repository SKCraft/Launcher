/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.dialog;

import com.google.common.base.Joiner;
import com.skcraft.launcher.builder.FeaturePattern;
import com.skcraft.launcher.builder.FnPatternList;
import com.skcraft.launcher.creator.model.swing.RecommendationComboBoxModel;
import com.skcraft.launcher.model.modpack.Feature;
import com.skcraft.launcher.model.modpack.Feature.Recommendation;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.swing.TextFieldPopupMenu;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class FeaturePatternDialog extends JDialog {

    private static final Joiner NEW_LINE_JOINER = Joiner.on("\n");

    private final JTextField nameText = new JTextField(20);
    private final JTextArea descArea = new JTextArea(3, 40);
    private final JComboBox recommendationCombo = new JComboBox(new RecommendationComboBoxModel());
    private final JCheckBox selectedCheck = new JCheckBox("Selected by default");
    private final JTextArea includeArea = new JTextArea(8, 40);
    private final JTextArea excludeArea = new JTextArea(3, 40);

    private final FeaturePattern pattern;
    private boolean saved = false;

    public FeaturePatternDialog(Window parent, FeaturePattern pattern) {
        super(parent, "Configure Feature", ModalityType.DOCUMENT_MODAL);

        this.pattern = pattern;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initComponents();
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);

        copyFrom();
    }

    private void initComponents() {
        nameText.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);
        descArea.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);
        includeArea.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);
        excludeArea.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);

        descArea.setFont(nameText.getFont());
        includeArea.setFont(nameText.getFont());
        excludeArea.setFont(nameText.getFont());

        JPanel container = new JPanel();
        container.setLayout(new MigLayout("insets dialog"));

        container.add(new JLabel("Feature Name:"));
        container.add(nameText, "span");

        container.add(new JLabel("Recommendation:"));
        container.add(recommendationCombo, "span");

        container.add(selectedCheck, "span");

        container.add(new JLabel("Description:"), "wrap");
        container.add(SwingHelper.wrapScrollPane(descArea), "span");

        container.add(new JLabel("Include Patterns:"), "wrap");
        container.add(SwingHelper.wrapScrollPane(includeArea), "span");

        container.add(new JLabel("Exclude Patterns:"), "wrap");
        container.add(SwingHelper.wrapScrollPane(excludeArea), "span, gapbottom unrel");

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        container.add(okButton, "tag ok, span, split 2, sizegroup bttn");
        container.add(cancelButton, "tag cancel, sizegroup bttn");

        getRootPane().setDefaultButton(okButton);
        getRootPane().registerKeyboardAction(e -> cancelButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        add(container, BorderLayout.CENTER);

        okButton.addActionListener(e -> {
            if (nameText.getText().trim().isEmpty()) {
                SwingHelper.showErrorDialog(FeaturePatternDialog.this, "The 'Feature Name' field cannot be empty.", "Input Error");
                return;
            }

            if (descArea.getText().trim().isEmpty()) {
                SwingHelper.showErrorDialog(FeaturePatternDialog.this, "The 'Description' field cannot be empty.", "Input Error");
                return;
            }

            if (includeArea.getText().trim().isEmpty()) {
                SwingHelper.showErrorDialog(FeaturePatternDialog.this, "The 'Include Patterns' field cannot be empty.", "Input Error");
                return;
            }

            copyTo();
            saved = true;
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());
    }

    private void copyFrom() {
        if (pattern.getFeature() == null) {
            pattern.setFeature(new Feature());
        }

        if (pattern.getFilePatterns() == null) {
            pattern.setFilePatterns(new FnPatternList());
        }

        SwingHelper.setTextAndResetCaret(nameText, pattern.getFeature().getName());
        SwingHelper.setTextAndResetCaret(descArea, pattern.getFeature().getDescription());
        recommendationCombo.setSelectedItem(pattern.getFeature().getRecommendation());
        selectedCheck.setSelected(pattern.getFeature().isSelected());
        SwingHelper.setTextAndResetCaret(includeArea, NEW_LINE_JOINER.join(pattern.getFilePatterns().getInclude()));
        SwingHelper.setTextAndResetCaret(excludeArea, NEW_LINE_JOINER.join(pattern.getFilePatterns().getExclude()));
    }

    private void copyTo() {
        pattern.getFeature().setName(nameText.getText().trim());
        pattern.getFeature().setDescription(descArea.getText().trim());
        pattern.getFeature().setRecommendation((Recommendation) recommendationCombo.getSelectedItem());
        pattern.getFeature().setSelected(selectedCheck.isSelected());
        pattern.getFilePatterns().setInclude(SwingHelper.linesToList(includeArea.getText()));
        pattern.getFilePatterns().setExclude(SwingHelper.linesToList(excludeArea.getText()));
    }

    public static boolean showEditor(Window window, FeaturePattern pattern) {
        FeaturePatternDialog dialog = new FeaturePatternDialog(window, pattern);
        dialog.setVisible(true);
        return dialog.saved;
    }

}
