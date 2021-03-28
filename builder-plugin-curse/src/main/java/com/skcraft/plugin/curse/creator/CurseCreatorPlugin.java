package com.skcraft.plugin.curse.creator;

import com.beust.jcommander.internal.Lists;
import com.skcraft.launcher.builder.plugin.BuilderPlugin;
import com.skcraft.launcher.creator.plugin.CreatorToolsPlugin;
import com.skcraft.launcher.creator.plugin.PluginMenu;
import com.skcraft.plugin.curse.CurseBuildPlugin;

import java.util.List;

public class CurseCreatorPlugin extends CreatorToolsPlugin {
	@Override
	public String getName() {
		return "SKCraft Curseforge Tools";
	}

	@Override
	public List<PluginMenu> getPluginMenus() {
		return Lists.newArrayList(new CurseModsDialog.Menu());
	}

	@Override
	public Class<? extends BuilderPlugin> getBuilderPlugin() {
		return CurseBuildPlugin.class;
	}
}
