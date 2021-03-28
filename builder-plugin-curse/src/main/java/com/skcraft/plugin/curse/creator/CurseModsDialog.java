package com.skcraft.plugin.curse.creator;

import com.skcraft.launcher.creator.model.creator.Pack;
import com.skcraft.launcher.creator.plugin.PluginMenu;
import com.skcraft.launcher.swing.LinedBoxPanel;
import com.skcraft.plugin.curse.model.CurseProject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CurseModsDialog extends JDialog {
	private final LinedBoxPanel panel = new LinedBoxPanel(false).fullyPadded();
	private final JList<CurseProject> searchResults = new JList<>();
	private final JList<CurseProject> selected = new JList<>();
	private final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, searchResults, selected);
	private final JButton addMod = new JButton("Add Mod >>");
	private final JButton removeMod = new JButton("Remove Mod");

	private final Pack pack;

	public CurseModsDialog(Window owner, Pack pack) {
		super(owner);
		this.pack = pack;

		initComponents();
		setMinimumSize(new Dimension(500, 250));
		pack();
		setLocationRelativeTo(owner);
	}

	private void initComponents() {
		splitPane.setDividerLocation(0.5D);
		splitPane.setDividerSize(10);

		LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);
		buttonsPanel.addElement(addMod);
		buttonsPanel.addGlue();
		buttonsPanel.addElement(removeMod);

		panel.addElement(splitPane);
		panel.addElement(buttonsPanel);

		add(panel);

		addMod.addActionListener(e -> {

		});
	}

	public static class Menu implements PluginMenu {
		@Override
		public String getTitle() {
			return "Add Mods";
		}

		@Override
		public boolean requiresPack() {
			return true;
		}

		@Override
		public void onOpen(Window owner, ActionEvent e, Pack pack) {
			CurseModsDialog dialog = new CurseModsDialog(owner, pack);
			dialog.setVisible(true);
		}
	}
}
