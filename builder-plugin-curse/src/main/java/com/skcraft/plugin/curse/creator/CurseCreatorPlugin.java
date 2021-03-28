package com.skcraft.plugin.curse.creator;

import com.skcraft.launcher.builder.plugin.BuilderPlugin;
import com.skcraft.launcher.creator.plugin.CreatorToolsPlugin;
import com.skcraft.plugin.curse.CurseBuildPlugin;

public class CurseCreatorPlugin extends CreatorToolsPlugin {
	@Override
	public Class<? extends BuilderPlugin> getBuilderPlugin() {
		return CurseBuildPlugin.class;
	}
}
