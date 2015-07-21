/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.buildtools.compile;

import com.skcraft.launcher.swing.DirectoryField;
import com.skcraft.launcher.swing.SwingHelper;
import lombok.Data;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class BuildDialog extends JDialog {

    private final DirectoryField destDirField = new DirectoryField();
    private final JTextField versionText = new JTextField(20);
    private final JTextField manifestFilenameText = new JTextField(30);
    private final JCheckBox cleanCheck = new JCheckBox("Delete previously generated files first");
    @Getter
    private BuildOptions options;

    public BuildDialog(Window parent) {
        super(parent, "Build Release", ModalityType.DOCUMENT_MODAL);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initComponents();
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel container = new JPanel();
        container.setLayout(new MigLayout("insets dialog"));

        container.add(new JLabel("Version:"));
        container.add(versionText, "span");

        container.add(new JLabel("Manifest Filename:"));
        container.add(manifestFilenameText, "span");

        container.add(new JLabel("Output Directory:"));
        container.add(destDirField, "span");

        container.add(cleanCheck, "span, gapbottom unrel");

        JButton buildButton = new JButton("Build...");
        JButton cancelButton = new JButton("Cancel");

        container.add(buildButton, "tag ok, span, split 2, sizegroup bttn");
        container.add(cancelButton, "tag cancel, sizegroup bttn");

        add(container, BorderLayout.CENTER);

        buildButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                returnValue();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void returnValue() {
        String version = versionText.getText().trim();
        String manifestFilename = manifestFilenameText.getText().trim();

        if (version.isEmpty()) {
            SwingHelper.showErrorDialog(this, "A version string must be entered.", "Error");
            return;
        }

        if (manifestFilename.isEmpty()) {
            SwingHelper.showErrorDialog(this, "A manifest filename must be entered.", "Error");
            return;
        }

        if (destDirField.getPath().isEmpty()) {
            SwingHelper.showErrorDialog(this, "A destination directory must be entered.", "Error");
            return;
        }

        options = new BuildOptions(version, manifestFilename, cleanCheck.isSelected(), new File(destDirField.getPath()));
        dispose();
    }

    public static BuildOptions showBuildDialog(Window parent, String version, String manifestName, File destDir) {
        BuildDialog dialog = new BuildDialog(parent);
        dialog.versionText.setText(version);
        dialog.manifestFilenameText.setText(manifestName);
        dialog.destDirField.setPath(destDir.getAbsolutePath());
        dialog.setVisible(true);
        return dialog.getOptions();
    }

    @Data
    public static class BuildOptions {
        private final String version;
        private final String manifestFilename;
        private final boolean clean;
        private final File destDir;
    }

}
