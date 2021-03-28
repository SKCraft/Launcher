package com.skcraft.plugin.curse;

import com.beust.jcommander.JCommander;
import com.skcraft.launcher.builder.BuilderOptions;
import com.skcraft.launcher.builder.plugin.Builder;
import com.skcraft.launcher.builder.plugin.BuilderPlugin;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;

@Log
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

		if (options.getCurseModsPath().isDirectory()) {
			log.info("Collecting Curse mods...");
			collector.walk(options.getCurseModsPath());
		} else {
			log.warning(String.format("%s isn't a directory, skipping", options.getCurseModsPath()));
		}
	}
}
