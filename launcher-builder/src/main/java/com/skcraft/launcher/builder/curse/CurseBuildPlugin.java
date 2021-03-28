package com.skcraft.launcher.builder.curse;

import com.beust.jcommander.JCommander;
import com.skcraft.launcher.builder.BuilderOptions;
import com.skcraft.launcher.builder.plugin.Builder;
import com.skcraft.launcher.builder.plugin.BuilderPlugin;

import java.io.File;

public class CurseBuildPlugin extends BuilderPlugin {
	private CurseOptions options = new CurseOptions();

	@Override
	public void acceptOptions(BuilderOptions builderOptions, String[] args) {
		new JCommander(this.options, args);

		if (options.getCachePath() == null) {
			options.setCachePath(new File(builderOptions.getInputPath(), "cache"));
		}
	}

	@Override
	public void onBuild(Builder builder) {

	}
}
