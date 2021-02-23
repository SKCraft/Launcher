package com.skcraft.launcher.model.loader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.skcraft.launcher.model.minecraft.Library;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MavenUrl {
	@JsonProperty("maven")
	private String name;
	private String url;
	private String version;
	private boolean stable;

	public Library toLibrary() {
		Library library = new Library();
		library.setName(name);
		library.setUrl(url);

		return library;
	}
}
