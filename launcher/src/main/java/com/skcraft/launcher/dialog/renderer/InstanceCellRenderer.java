/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.dialog.renderer;

import com.skcraft.launcher.Instance;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.swing.SwingHelper;

import javax.swing.*;
import java.awt.*;

public class InstanceCellRenderer extends DefaultListCellRenderer {

    private final Icon instanceIcon;
    private final Icon customInstanceIcon;
    private final Icon downloadIcon;

    public InstanceCellRenderer() {
        instanceIcon = SwingHelper.createIcon(Launcher.class, "instance_icon.png", 16, 16);
        customInstanceIcon = SwingHelper.createIcon(Launcher.class, "custom_instance_icon.png", 16, 16);
        downloadIcon = SwingHelper.createIcon(Launcher.class, "download_icon.png", 14, 14);
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Instance instance = (Instance) value;
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        // Add some padding to the cell
        label.setBorder(BorderFactory.createCompoundBorder(label.getBorder(), BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        label.setIcon(getInstanceIcon(instance));
        return label;
    }

    private Icon getInstanceIcon(Instance instance) {
        if (!instance.isLocal()) {
            return downloadIcon;
        } else if (instance.getManifestURL() != null) {
            return instanceIcon;
        } else {
            return customInstanceIcon;
        }
    }

}
