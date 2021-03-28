package com.skcraft.launcher.creator.dialog;

import com.skcraft.launcher.creator.Creator;
import com.skcraft.launcher.creator.model.creator.Pack;
import com.skcraft.launcher.creator.model.swing.PluginsTableModel;
import com.skcraft.launcher.swing.CheckboxTable;
import com.skcraft.launcher.swing.LinedBoxPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PluginSelectionDialog extends JDialog {
	private final LinedBoxPanel panel = new LinedBoxPanel(false).fullyPadded();
	private final JLabel title = new JLabel("Select which plugins to use while building this pack.");
	private final CheckboxTable pluginsTable = new CheckboxTable();
	private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);
	private final JButton okButton = new JButton("OK");

	private List<PluginsTableModel.PluginModel> plugins;

	public PluginSelectionDialog(Window owner, Creator creator, Pack pack) {
		super(owner, "Enable Plugins", ModalityType.DOCUMENT_MODAL);

		this.plugins = creator.getPlugins().stream()
				.map(wrapper -> {
					PluginsTableModel.PluginModel model = new PluginsTableModel.PluginModel(wrapper.getInfo().getId());
					model.setEnabled(pack.getEnabledPlugins().contains(model.getPluginId()));
					return model;
				})
				.collect(Collectors.toList());

		setTitle("Select plugins to enable");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		initComponents();
		setMinimumSize(new Dimension(400, 140));
		pack();
		setLocationRelativeTo(owner);
	}

	private void initComponents() {
		pluginsTable.setModel(new PluginsTableModel(plugins));
		pluginsTable.setRowSelectionAllowed(false);

		okButton.setMargin(new Insets(0, 10, 0, 10));

		buttonsPanel.addGlue();
		buttonsPanel.addElement(okButton);

		panel.addElement(title);
		panel.addElement(pluginsTable);
		panel.addGlue();
		panel.addElement(buttonsPanel);

		add(panel);

		okButton.addActionListener(e -> PluginSelectionDialog.this.dispose());
	}

	public static void showPluginDialog(Window owner, Creator creator, Pack pack) {
		PluginSelectionDialog dialog = new PluginSelectionDialog(owner, creator, pack);
		dialog.setVisible(true);

		Set<String> enabled = dialog.plugins.stream()
				.filter(PluginsTableModel.PluginModel::isEnabled)
				.map(PluginsTableModel.PluginModel::getPluginId)
				.collect(Collectors.toSet());

		pack.setEnabledPlugins(enabled);
	}
}
