package com.skcraft.launcher.creator.plugin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatorPluginInfo {
	private String id;
	private String pluginClass;
}
