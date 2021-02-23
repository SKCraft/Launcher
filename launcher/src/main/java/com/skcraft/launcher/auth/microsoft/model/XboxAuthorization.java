package com.skcraft.launcher.auth.microsoft.model;

import lombok.Data;

@Data
public class XboxAuthorization {
	private final String token;
	private final String uhs;

	public String getCombinedToken() {
		return String.format("%s;%s", uhs, token);
	}
}
