/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.dialog;

import com.skcraft.launcher.swing.DirectoryField;
import com.skcraft.launcher.swing.SwingHelper;
import lombok.Data;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;

public class DeployServerDialog extends JDialog {

    private final DirectoryField destDirField = new DirectoryField();
    private final JCheckBox cleanModsCheck = new JCheckBox("Delete \"mods/\" folder before deploying");
    @Getter
    private DeployOptions options;

    public DeployServerDialog(Window parent) {
        super(parent, "Deploy Server Files", ModalityType.DOCUMENT_MODAL);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initComponents();
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);

        cleanModsCheck.setSelected(true);
    }

    private void initComponents() {
        JPanel container = new JPanel();
        container.setLayout(new MigLayout("insets dialog"));

        container.add(new JLabel("Output Directory:"));
        container.add(destDirField, "span");

        container.add(cleanModsCheck, "span, gapbottom unrel");

        JButton buildButton = new JButton("Deploy");
        JButton cancelButton = new JButton("Cancel");

        container.add(buildButton, "tag ok, span, split 2, sizegroup bttn");
        container.add(cancelButton, "tag cancel, sizegroup bttn");

        add(container, BorderLayout.CENTER);

        getRootPane().setDefaultButton(buildButton);
        getRootPane().registerKeyboardAction(e -> cancelButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        buildButton.addActionListener(e -> returnValue());
        cancelButton.addActionListener(e -> dispose());
    }

    private void returnValue() {
        String dir = destDirField.getPath();

        if (dir.isEmpty()) {
            SwingHelper.showErrorDialog(this, "A directory must be entered.", "Error");
            return;
        }

        File dirFile = new File(dir);

        if (!dirFile.isDirectory()) {
            SwingHelper.showErrorDialog(this, "The selected path is not a directory that exists.", "Error");
            return;
        }

        options = new DeployOptions(dirFile,cleanModsCheck.isSelected());
        dispose();
    }

    public static DeployOptions showDeployDialog(Window parent, File destDir) {
        DeployServerDialog dialog = new DeployServerDialog(parent);
        if (destDir != null) {
            dialog.destDirField.setPath(destDir.getAbsolutePath());
        }
        dialog.setVisible(true);
        return dialog.getOptions();
    }

    @Data
    public static class DeployOptions {
        private final File destDir;
        private final boolean cleanMods;
    }

}
