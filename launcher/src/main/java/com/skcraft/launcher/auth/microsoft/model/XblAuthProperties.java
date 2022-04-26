package com.skcraft.launcher.auth.microsoft.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NonNull;

@Data
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class XblAuthProperties {
	private String authMethod = "RPS";
	private String siteName = "user.auth.xboxlive.com";
	@NonNull
	private String rpsTicket;
}
