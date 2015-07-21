/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.buildtools.compile;

import com.skcraft.launcher.swing.SwingHelper;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ProblemViewer extends JDialog {

    private static final String DEFAULT_EXPLANATION = "Select a problem on the left to see the explanation here.";
    private final ProblemTable problemTable = new ProblemTable();
    private final ProblemTableModel problemTableModel;
    private final JTextArea explanationText = new JTextArea(DEFAULT_EXPLANATION);

    public ProblemViewer(Window parent, List<Problem> problems) {
        super(parent, "Potential Problems", ModalityType.DOCUMENT_MODAL);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initComponents();
        pack();
        setLocationRelativeTo(parent);

        problemTableModel = new ProblemTableModel(problems);
        problemTable.setModel(problemTableModel);
    }

    private void initComponents() {
        explanationText.setFont(new JTextField().getFont());
        explanationText.setEditable(false);
        explanationText.setLineWrap(true);
        explanationText.setWrapStyleWord(true);

        JPanel container = new JPanel();
        container.setLayout(new MigLayout("fill, insets dialog"));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                SwingHelper.wrapScrollPane(problemTable), SwingHelper.
                wrapScrollPane(explanationText));
        splitPane.setDividerLocation(180);
        SwingHelper.flattenJSplitPane(splitPane);

        container.add(splitPane, "grow, w 220:500, h 240, wrap");

        JButton closeButton = new JButton("Close");
        container.add(closeButton, "tag cancel, gaptop unrel");

        add(container, BorderLayout.CENTER);

        problemTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Problem selected = problemTableModel.getProblem(problemTable.getSelectedRow());
                if (selected != null) {
                    SwingHelper.setTextAndResetCaret(explanationText, selected.getExplanation());
                }
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
}
