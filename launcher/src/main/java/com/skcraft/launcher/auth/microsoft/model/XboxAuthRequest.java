package com.skcraft.launcher.auth.microsoft.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NonNull;

@Data
@JsonNaming(PropertyNamingStrategy.PascalCaseStrategy.class)
public class XboxAuthRequest<T> {
	@NonNull private T properties;
	private String relyingParty = "http://auth.xboxlive.com";
	private String tokenType = "JWT";
}
