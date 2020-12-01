package com.skcraft.launcher.model.loader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.skcraft.launcher.model.minecraft.Side;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(staticName = "create")
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SidedData {
	private String client;
	private String server;

	public String resolveFor(Side side) {
		switch (side) {
			case CLIENT: return client;
			case SERVER: return server;
			default: return null;
		}
	}

	public static SidedData of(String singleValue) {
		return new SidedData(singleValue, singleValue);
	}
}
