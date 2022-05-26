package com.skcraft.launcher;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.skcraft.launcher.launch.MemorySettings;
import com.skcraft.launcher.launch.runtime.JavaRuntime;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstanceSettings {
	private JavaRuntime runtime;
	private MemorySettings memorySettings;
	private String customJvmArgs;
}
