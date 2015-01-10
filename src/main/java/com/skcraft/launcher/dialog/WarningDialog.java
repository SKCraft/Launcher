package com.skcraft.launcher.dialog;

import com.skcraft.launcher.swing.FormPanel;
import com.skcraft.launcher.swing.LinedBoxPanel;
import com.skcraft.launcher.update.SelfInstallUpdate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author dags_ <dags@dags.me>
 */

public class WarningDialog extends JDialog
{
    private final JButton accept = new JButton();
    private final JButton cancel = new JButton();
    private final JTextArea blurb = new JTextArea();

    private final FormPanel formPanel = new FormPanel();
    private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);

    private final JDialog parent;

    public WarningDialog(Window owner, JDialog parent)
    {
        super(owner, ModalityType.DOCUMENT_MODAL);
        this.parent = parent;
        setTitle("Are You Sure?");
        initComponents();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(340, 250));
        setResizable(false);
        setLocationRelativeTo(owner);

        pack();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent event)
            {
                dispose();
            }
        });
    }

    private void initComponents()
    {
        blurb.setEditable(false);
        blurb.setOpaque(true);
        blurb.setLineWrap(true);
        blurb.setWrapStyleWord(true);
        blurb.setEnabled(false);
        blurb.append("Warning!\nThis install method will delete some existing ArdaCraft Modpack folders. \n\n");
        blurb.append("To avoid losing any game data, close the launcher and move it to an empty folder before running.\n");
        formPanel.addRow(blurb);

        buttonsPanel.addGlue();
        accept.setText("Accept");
        cancel.setText("Cancel");
        accept.setMinimumSize(new Dimension(150, 20));
        cancel.setMinimumSize(new Dimension(150, 20));
        buttonsPanel.addElement(accept);
        buttonsPanel.addElement(cancel);

        add(formPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(accept);

        accept.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                SelfInstallUpdate update = new SelfInstallUpdate(getOwner(), parent);
                update.download();
                dispose();
            }
        });
        cancel.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        });
    }
}
