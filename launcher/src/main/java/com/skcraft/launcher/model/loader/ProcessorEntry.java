package com.skcraft.launcher.model.loader;

import com.google.common.collect.Maps;
import com.skcraft.launcher.install.InstallLog;
import com.skcraft.launcher.install.Installer;
import com.skcraft.launcher.install.ProcessorTask;
import com.skcraft.launcher.install.UpdateCache;
import com.skcraft.launcher.model.minecraft.Side;
import com.skcraft.launcher.model.modpack.DownloadableFile;
import com.skcraft.launcher.model.modpack.ManifestEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProcessorEntry extends ManifestEntry {
	private String loaderName;
	private InstallProcessor processor;

	@Override
	public void install(Installer installer, InstallLog log, UpdateCache cache, File contentDir) throws Exception {
		LoaderManifest loaderManifest = getManifest().getLoaders().get(loaderName);

		HashMap<String, DownloadableFile.LocalFile> localFilesMap = Maps.newHashMap();
		for (DownloadableFile downloadableFile : loaderManifest.getDownloadableFiles()) {
			if (downloadableFile.getSide() != Side.CLIENT) continue;

			DownloadableFile.LocalFile localFile = downloadableFile.download(installer, getManifest());

			localFilesMap.put(localFile.getName(), localFile);
		}

		installer.queueLate(new ProcessorTask(processor, loaderManifest, getManifest(), localFilesMap));
	}
}
