package com.skcraft.plugin.curse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurseMod {
	private String projectId;
	private String fileId;
}
