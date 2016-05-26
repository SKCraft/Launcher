/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.model.swing;

import com.skcraft.launcher.creator.Creator;
import com.skcraft.launcher.creator.model.creator.Problem;
import com.skcraft.launcher.swing.SwingHelper;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ProblemTableModel extends AbstractTableModel {

    private static final Icon WARNING_ICON;

    static {
        WARNING_ICON = SwingHelper.createIcon(Creator.class, "warning_icon.png");
    }

    private final List<Problem> problems;

    public ProblemTableModel(List<Problem> problems) {
        this.problems = problems;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "";
            case 1:
                return "Problem";
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Icon.class;
            case 1:
                return Problem.class;
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public int getRowCount() {
        return problems.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return WARNING_ICON;
            case 1:
                return problems.get(rowIndex);
            default:
                return null;
        }
    }

    public Problem getProblem(int index) {
        return problems.get(index);
    }

}
