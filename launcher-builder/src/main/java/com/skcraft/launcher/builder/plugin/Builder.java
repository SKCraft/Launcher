package com.skcraft.launcher.builder.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skcraft.launcher.builder.PropertiesApplicator;
import com.skcraft.launcher.model.modpack.Manifest;

/**
 * Builder passed to plugins to allow restricted access
 */
public interface Builder {
	ObjectMapper getMapper();
	Manifest getManifest();
	PropertiesApplicator getApplicator();
}
