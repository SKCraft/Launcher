package com.skcraft.launcher.model.loader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BasicInstallProfile {
	private String profile;
	private int spec;

	@JsonProperty("install")
	private Legacy legacyProfile;

	@JsonIgnore
	public boolean isLegacy() {
		return getLegacyProfile() != null;
	}

	public String resolveProfileName() {
		if (isLegacy()) {
			return getLegacyProfile().getProfileName();
		} else {
			return getProfile();
		}
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Legacy {
		private String profileName;
	}
}
