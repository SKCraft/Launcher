package com.skcraft.plugin.curse.model;

import com.skcraft.plugin.curse.CurseMod;
import lombok.Data;

import java.io.File;

/**
 * Represents mod metadata cached in-memory for the purpose of rendering the "Add Mods" screen.
 */
@Data
public class LoadedMod implements ProjectHolder {
	private final CurseMod mod;
	private final CurseProject project;

	public File getDiskLocation(File curseModsDir) {
		return new File(curseModsDir, String.format("%s.json", project.getSlug()));
	}
}
