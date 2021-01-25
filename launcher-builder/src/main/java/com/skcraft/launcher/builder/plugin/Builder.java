package com.skcraft.launcher.builder.plugin;

import com.skcraft.launcher.model.modpack.Manifest;

/**
 * Builder passed to plugins to allow restricted access
 */
public interface Builder {
	Manifest getManifest();
}
