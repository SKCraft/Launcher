/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import javax.swing.*;
import java.awt.*;

public class FormPanel extends JPanel {

    private static final GridBagConstraints labelConstraints;
    private static final GridBagConstraints fieldConstraints;
    private static final GridBagConstraints fieldConstraints2;
    
    private static final GridBagConstraints wideFieldConstraints;

    private final GridBagLayout layout;

    static {
        fieldConstraints = new GridBagConstraints();
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.gridwidth = GridBagConstraints.REMAINDER;
        fieldConstraints.insets = new Insets(5, 5, 2, 5);
        
        fieldConstraints2 = new GridBagConstraints();
        fieldConstraints2.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints2.weightx = 1.0;
        fieldConstraints2.insets = new Insets(5, 5, 2, 5);

        labelConstraints = (GridBagConstraints) fieldConstraints.clone();
        labelConstraints.weightx = 0.0;
        labelConstraints.gridwidth = 1;
        labelConstraints.insets = new Insets(4, 5, 1, 10);

        wideFieldConstraints = (GridBagConstraints) fieldConstraints.clone();
        wideFieldConstraints.insets = new Insets(7, 2, 1, 2);
    }

    public FormPanel() {
        setLayout(layout = new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    public void addRow(Component label, Component component) {
        add(label);
        add(component);
        layout.setConstraints(label, labelConstraints);
        layout.setConstraints(component, fieldConstraints);
    }

    public void addRow(Component component) {
        add(component);
        layout.setConstraints(component, wideFieldConstraints);
    }
    
    public void addRow(Component label, Component component, Component component2) {
        add(label);
        JPanel jPanel = new JPanel();
        jPanel.add(component);
        jPanel.add(component2);
        //jPanel.add(component2);
        add(jPanel);
        layout.setConstraints(label, labelConstraints);
        layout.setConstraints(jPanel, fieldConstraints);
    }

}
