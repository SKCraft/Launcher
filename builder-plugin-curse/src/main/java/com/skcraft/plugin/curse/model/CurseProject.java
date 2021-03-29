package com.skcraft.plugin.curse.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.skcraft.plugin.curse.CurseMod;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurseProject implements ProjectHolder {
	private String id;
	private String name;
	private String websiteUrl;
	private String summary;
	private String slug;
	private List<GameVersionFile> gameVersionLatestFiles;
	private List<Attachment> attachments;

	@Override
	public CurseProject getProject() {
		return this;
	}

	public GameVersionFile findFileForVersion(String version) {
		for (GameVersionFile file : gameVersionLatestFiles) {
			if (file.getGameVersion().equals(version)) {
				return file;
			}
		}

		return null;
	}

	public LoadedMod toLoadedMod(GameVersionFile versionFile) {
		CurseMod mod = new CurseMod(id, versionFile.getProjectFileId(), null);
		return new LoadedMod(mod, this);
	}

	public Attachment getDefaultIcon() {
		for (Attachment attachment : attachments) {
			if (attachment.isDefault()) {
				return attachment;
			}
		}

		if (!attachments.isEmpty()) {
			return attachments.get(0);
		}

		return null;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Attachment {
		private String title;
		private String thumbnailUrl;
		private String url;

		@JsonProperty("isDefault")
		private boolean isDefault;
	}
}
