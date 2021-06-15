package com.skcraft.launcher;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.skcraft.launcher.launch.JavaRuntime;
import com.skcraft.launcher.launch.MemorySettings;
import lombok.Data;

import java.util.Optional;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstanceSettings {
	private JavaRuntime runtime;
	private MemorySettings memorySettings;
	private String customJvmArgs;

	/**
	 * @return Empty optional if there is no custom runtime set, present optional if there is.
	 */
	public Optional<JavaRuntime> getRuntime() {
		return Optional.ofNullable(runtime);
	}

	/**
	 * @return Empty optional if there are no custom memory settings, present optional if there are.
	 */
	public Optional<MemorySettings> getMemorySettings() {
		return Optional.ofNullable(memorySettings);
	}
}
