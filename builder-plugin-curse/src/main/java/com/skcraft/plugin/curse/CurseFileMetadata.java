package com.skcraft.plugin.curse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurseFileMetadata {
	private String id;
	private String displayName;
	private String fileName;
	private String fileDate;
	private long fileLength;
	private String downloadUrl;
}
