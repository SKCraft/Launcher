package com.skcraft.launcher.auth.microsoft.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class McServicesError {
	private String error;
	private String errorMessage;
}
