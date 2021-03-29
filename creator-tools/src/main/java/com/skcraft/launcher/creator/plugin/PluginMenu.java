package com.skcraft.launcher.creator.plugin;

import com.skcraft.launcher.creator.model.creator.Pack;

import java.awt.event.ActionEvent;

public interface PluginMenu {
	String getTitle();

	/**
	 * Return true if {@link PluginMenu#onOpen(MenuContext, ActionEvent, Pack)} should only be called
	 * if the user has selected a pack.
	 * @return True to require a pack, false if you don't need one.
	 */
	boolean requiresPack();

	/**
	 * Called when the menu item was clicked.
	 * @param ctx Context object with various useful objects.
	 * @param e Action event that triggered this call.
	 * @param pack Pack to operate on; guaranteed to be non-null if {@link PluginMenu#requiresPack()} returns true.
	 */
	void onOpen(MenuContext ctx, ActionEvent e, Pack pack);
}
