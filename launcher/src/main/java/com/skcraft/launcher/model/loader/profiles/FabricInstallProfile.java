package com.skcraft.launcher.model.loader.profiles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.skcraft.launcher.model.loader.ExtendedSidedData;
import com.skcraft.launcher.model.loader.SidedData;
import com.skcraft.launcher.model.minecraft.Library;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FabricInstallProfile {
	private int version;
	private Library loader;
	private ExtendedSidedData<List<Library>> libraries;
	private SidedData<String> mainClass;
}
