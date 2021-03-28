package com.skcraft.launcher.creator.model.swing;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.swing.table.AbstractTableModel;
import java.util.List;

@RequiredArgsConstructor
public class PluginsTableModel extends AbstractTableModel {
	private final List<PluginModel> plugins;

	@Override
	public int getRowCount() {
		return plugins.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return plugins.get(rowIndex).isEnabled();
		} else {
			return plugins.get(rowIndex).getPluginId();
		}
	}

	@Override
	public String getColumnName(int column) {
		if (column == 1) {
			return "Plugin";
		}

		return null;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
			case 0:
				return Boolean.class;
			case 1:
				return String.class;
			default:
				return null;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 0;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			plugins.get(rowIndex).setEnabled((Boolean) aValue);
		}
	}

	@Data
	public static class PluginModel {
		private final String pluginId;
		private boolean enabled;
	}
}
