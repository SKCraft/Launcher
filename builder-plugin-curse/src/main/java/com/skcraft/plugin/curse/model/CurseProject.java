package com.skcraft.plugin.curse.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurseProject {
	private String id;
	private String name;
	private String websiteUrl;
	private String summary;
	private String slug;
	private List<GameVersionFile> gameVersionLatestFiles;

	public GameVersionFile findFileForVersion(String version) {
		for (GameVersionFile file : gameVersionLatestFiles) {
			if (file.getGameVersion().equals(version)) {
				return file;
			}
		}

		return null;
	}
}
