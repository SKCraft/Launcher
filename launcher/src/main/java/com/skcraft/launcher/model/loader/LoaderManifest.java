package com.skcraft.launcher.model.loader;

import com.skcraft.launcher.model.modpack.DownloadableFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoaderManifest {
	private Map<String, SidedData> sidedData;
	private List<DownloadableFile> downloadableFiles;
}
