package com.skcraft.plugin.curse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.skcraft.launcher.model.modpack.Feature;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurseMod {
	private String projectId;
	private String fileId;
	private Feature feature;
}
