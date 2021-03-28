package com.skcraft.plugin.curse;

import com.beust.jcommander.JCommander;
import com.skcraft.launcher.builder.BuilderOptions;
import com.skcraft.launcher.builder.plugin.Builder;
import com.skcraft.launcher.builder.plugin.BuilderPlugin;

import java.io.File;
import java.io.IOException;

public class CurseBuildPlugin extends BuilderPlugin {
	private CurseOptions options = new CurseOptions();

	@Override
	public void acceptOptions(BuilderOptions builderOptions, String[] args) {
		JCommander commander = new JCommander(this.options);
		commander.setAcceptUnknownOptions(true);
		commander.parse(args);

		if (options.getCachePath() == null) {
			builderOptions.requireInputPath("--cache");
			options.setCachePath(new File(builderOptions.getInputPath(), "cache"));
		}

		if (options.getCurseModsPath() == null) {
			builderOptions.requireInputPath("--curse-mods");
			options.setCurseModsPath(new File(builderOptions.getInputPath(), "cursemods"));
		}
	}

	@Override
	public void onBuild(Builder builder) throws IOException {
		CurseCachingResolver resolver = new CurseCachingResolver(options.getCachePath(), builder.getMapper());
		CurseModCollector collector = new CurseModCollector(builder.getManifest(), builder.getApplicator(), resolver,
				builder.getMapper());

		collector.walk(options.getCurseModsPath());
	}
}
