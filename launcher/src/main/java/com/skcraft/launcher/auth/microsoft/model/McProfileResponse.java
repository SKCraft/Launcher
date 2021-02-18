package com.skcraft.launcher.auth.microsoft.model;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class McProfileResponse {
	@JsonProperty("id") private String uuid;
	private String name;
	private List<Skin> skins = Lists.newArrayList();

	public Skin getActiveSkin() {
		return skins.stream().filter(skin -> skin.getState().equals("ACTIVE")).findFirst().orElse(null);
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Skin {
		private String state;
		private String url;
		private String variant;
	}
}
