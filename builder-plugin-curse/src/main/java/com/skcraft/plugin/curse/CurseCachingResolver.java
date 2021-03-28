package com.skcraft.plugin.curse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skcraft.launcher.util.HttpRequest;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;

import static com.skcraft.launcher.util.HttpRequest.url;

@RequiredArgsConstructor
public class CurseCachingResolver {
	private final File cacheDir;
	private final ObjectMapper mapper;

	public CurseFileMetadata resolveMetadata(CurseMod mod) throws IOException, InterruptedException {
		CurseFileMetadata cached = getCachedMetadata(mod.getFileId());
		if (cached != null) return cached;

		String apiUrl = String.format("https://addons-ecs.forgesvc.net/api/v2/addon/%s/file/%s",
				mod.getProjectId(), mod.getFileId());

		return HttpRequest.get(url(apiUrl))
				.execute()
				.expectResponseCode(200)
				.returnContent()
				.saveContent(getMetadataFile(mod.getFileId()))
				.asJson(CurseFileMetadata.class);
	}

	public File resolveModFile(CurseFileMetadata metadata) throws IOException, InterruptedException {
		File cachedMod = getModFile(metadata.getId());

		if (!cachedMod.exists()) {
			HttpRequest.get(url(metadata.getDownloadUrl()))
					.execute()
					.expectResponseCode(200)
					.saveContent(cachedMod);
		}

		return cachedMod;
	}

	public CurseFileMetadata getCachedMetadata(String fileId) throws IOException {
		File target = getMetadataFile(fileId);

		if (!target.exists()) return null;

		return mapper.readValue(target, CurseFileMetadata.class);
	}

	private File getMetadataFile(String fileId) {
		return new File(cacheDir, String.format("meta_%s.json", fileId));
	}

	private File getModFile(String fileId) {
		return new File(cacheDir, String.format("%s.jar", fileId));
	}
}
