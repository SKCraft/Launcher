package com.skcraft.launcher.auth.microsoft.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.PascalCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class XboxAuthResponse {
	private String token;
	private DisplayClaims displayClaims;

	@JsonIgnore
	public String getUhs() {
		return getDisplayClaims().getXui().get(0).getUhs();
	}

	@Data
	public static class DisplayClaims {
		private List<UhsContainer> xui;
	}

	@Data
	public static class UhsContainer {
		private String uhs;
	}
}
