/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.buildtools;

import com.skcraft.launcher.swing.DirectoryField;
import com.skcraft.launcher.swing.SwingHelper;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ProjectDirectoryDialog extends JDialog {

    private final DirectoryField directoryField = new DirectoryField();
    private File projectDir;

    public ProjectDirectoryDialog(Window parent) {
        super(parent, "Select Modpack Directory", ModalityType.DOCUMENT_MODAL);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initComponents();
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel container = new JPanel();
        container.setLayout(new MigLayout("insets dialog"));

        container.add(new JLabel("<html>Please select the project directory."), "wrap");
        container.add(directoryField, "span");

        JButton openButton = new JButton("Open");
        JButton cancelButton = new JButton("Cancel");

        container.add(openButton, "tag ok, span, split 2, sizegroup bttn, gaptop unrel");
        container.add(cancelButton, "tag cancel, sizegroup bttn");

        add(container, BorderLayout.CENTER);

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = directoryField.getPath();
                if (path.isEmpty()) {
                    SwingHelper.showErrorDialog(ProjectDirectoryDialog.this, "Please select a directory.", "No Directory");
                    return;
                }

                projectDir = new File(path);
                dispose();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    public static File showDirectoryDialog(Window parent, File initialDir) {
        ProjectDirectoryDialog dialog = new ProjectDirectoryDialog(parent);
        dialog.directoryField.setPath(initialDir.getAbsolutePath());
        dialog.setVisible(true);
        return dialog.projectDir;
    }

}
