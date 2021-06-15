package com.skcraft.launcher.launch;

import lombok.Data;

/**
 * Settings for launched process memory allocation.
 */
@Data
public class MemorySettings {
	/**
	 * Minimum memory in megabytes.
	 */
	private int minMemory;

	/**
	 * Maximum memory in megabytes.
	 */
	private int maxMemory;
}
