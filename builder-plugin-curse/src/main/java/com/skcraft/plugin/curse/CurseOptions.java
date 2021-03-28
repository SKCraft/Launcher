package com.skcraft.plugin.curse;

import com.beust.jcommander.Parameter;
import lombok.Data;

import java.io.File;

@Data
public class CurseOptions {
	@Parameter(names = "--curse-mods", description = "Folder of curse mod JSONs")
	private File curseModsPath;

	@Parameter(names = "--cache", description = "Path to cache folder where downloaded artifacts are stored.")
	private File cachePath;

	@Parameter(names = "--import-curse-manifest", description = "Import mods and loaders from a Curse manifest JSON")
	private File curseManifestLocation;
}
