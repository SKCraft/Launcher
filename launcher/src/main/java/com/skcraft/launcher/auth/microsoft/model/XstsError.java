package com.skcraft.launcher.auth.microsoft.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.PascalCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class XstsError {
	private long xErr;
	private String message;
	private String redirect;
}
