package com.skcraft.launcher.auth.microsoft.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.PascalCaseStrategy.class)
public class XstsAuthProperties {
	private String sandboxId = "RETAIL";
	private List<String> userTokens;

	public XstsAuthProperties(String token) {
		this.userTokens = Collections.singletonList(token);
	}
}
