package com.skcraft.launcher.auth.microsoft.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NonNull;

@Data
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class XboxAuthRequest<T> {
	@NonNull private T properties;
	private String relyingParty = "http://auth.xboxlive.com";
	private String tokenType = "JWT";
}
