package com.skcraft.launcher.model.loader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuiltMod implements Versionable {
	@JsonProperty("schema_version")
	private int schemaVersion;

	@JsonProperty("quilt_loader")
	private Mod meta;

	@Override
	public String getVersion() {
		return meta.getVersion();
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Mod {
		private String version;

		@JsonProperty("intermediate_mappings")
		private String intermediateMappings;
	}
}
