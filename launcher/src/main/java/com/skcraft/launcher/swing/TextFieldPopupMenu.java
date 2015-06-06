/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import com.skcraft.launcher.util.SharedLocale;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TextFieldPopupMenu extends JPopupMenu implements ActionListener {

    public static final TextFieldPopupMenu INSTANCE = new TextFieldPopupMenu();

    private final JMenuItem cutItem;
    private final JMenuItem copyItem;
    private final JMenuItem pasteItem;
    private final JMenuItem deleteItem;
    private final JMenuItem selectAllItem;

    private TextFieldPopupMenu() {
        cutItem = addMenuItem(new JMenuItem(SharedLocale.tr("context.cut"), 'T'));
        copyItem = addMenuItem(new JMenuItem(SharedLocale.tr("context.copy"), 'C'));
        pasteItem = addMenuItem(new JMenuItem(SharedLocale.tr("context.paste"), 'P'));
        deleteItem = addMenuItem(new JMenuItem(SharedLocale.tr("context.delete"), 'D'));
        addSeparator();
        selectAllItem = addMenuItem(new JMenuItem(SharedLocale.tr("context.selectAll"), 'A'));
    }

    private JMenuItem addMenuItem(JMenuItem item) {
        item.addActionListener(this);
        return add(item);
    }

    @Override
    public void show(Component invoker, int x, int y) {
        JTextComponent textComponent = (JTextComponent) invoker;
        boolean editable = textComponent.isEditable() && textComponent.isEnabled();
        cutItem.setVisible(editable);
        pasteItem.setVisible(editable);
        deleteItem.setVisible(editable);
        super.show(invoker, x, y);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextComponent textComponent = (JTextComponent) getInvoker();
        textComponent.requestFocus();

        boolean haveSelection =
                textComponent.getSelectionStart() != textComponent.getSelectionEnd();

        if (e.getSource() == cutItem) {
            if (!haveSelection) textComponent.selectAll();
            textComponent.cut();
        } else if (e.getSource() == copyItem) {
            if (!haveSelection) textComponent.selectAll();
            textComponent.copy();
        } else if (e.getSource() == pasteItem) {
            textComponent.paste();
        } else if (e.getSource() == deleteItem) {
            if (!haveSelection) textComponent.selectAll();
            textComponent.replaceSelection("");
        } else if (e.getSource() == selectAllItem) {
            textComponent.selectAll();
        }
    }
}