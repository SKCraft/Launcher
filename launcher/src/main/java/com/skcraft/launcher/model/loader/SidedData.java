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
public class SidedData<T> {
	private T client;
	private T server;

	public T resolveFor(Side side) {
		switch (side) {
			case CLIENT: return client;
			case SERVER: return server;
			default: return null;
		}
	}

	public static <T> SidedData<T> of(T singleValue) {
		return new SidedData<T>(singleValue, singleValue);
	}
}
