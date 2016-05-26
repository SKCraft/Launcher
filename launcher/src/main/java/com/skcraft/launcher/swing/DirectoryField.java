/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class DirectoryField extends JPanel {

    private static final long serialVersionUID = 5706210803738919578L;

    private final JTextField textField;
    private final JButton browseButton;

    public DirectoryField() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        textField = new JTextField(30);
        textField.setMaximumSize(textField.getPreferredSize());
        add(textField);

        add(Box.createHorizontalStrut(3));

        browseButton = new JButton("Browse...");
        browseButton.setPreferredSize(new Dimension(
                browseButton.getPreferredSize().width,
                textField.getPreferredSize().height));
        add(browseButton);

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browse();
            }
        });

        textField.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);
    }

    public JTextField getTextField() {
        return textField;
    }

    public JButton getBrowseButton() {
        return browseButton;
    }

    public void setPath(String path) {
        getTextField().setText(path);
    }

    public String getPath() {
        return getTextField().getText();
    }

    protected JFileChooser getFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) return true;
                return false;
            }

            @Override
            public String getDescription() {
                return "Directories";
            }
        });

        return chooser;
    }

    public void browse() {
        JFileChooser chooser = getFileChooser();
        File f = new File(getPath());
        if (f.exists() && f.isFile()) {
            f = f.getParentFile();
        }
        chooser.setCurrentDirectory(f);

        int returnVal = chooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            setPath(chooser.getSelectedFile().getPath());
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        getTextField().setEnabled(enabled);
        getBrowseButton().setEnabled(enabled);
    }

}
