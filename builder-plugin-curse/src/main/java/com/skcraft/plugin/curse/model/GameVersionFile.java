package com.skcraft.plugin.curse.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameVersionFile {
	private String gameVersion;
	private String projectFileId;
	private String projectFileName;
}
