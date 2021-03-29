package com.skcraft.plugin.curse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skcraft.launcher.builder.DirectoryWalker;
import com.skcraft.launcher.builder.FileInfoScanner;
import com.skcraft.launcher.builder.PropertiesApplicator;
import com.skcraft.launcher.model.modpack.FileInstall;
import com.skcraft.launcher.model.modpack.Manifest;
import com.skcraft.plugin.curse.model.CurseFileMetadata;
import com.skcraft.plugin.curse.model.CurseMod;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;

@Log
@RequiredArgsConstructor
public class CurseModCollector extends DirectoryWalker {
	private final Manifest manifest;
	private final PropertiesApplicator applicator;
	private final CurseCachingResolver cache;
	private final ObjectMapper mapper;

	@Override
	protected void onFile(File file, String relPath) throws IOException {
		try {
			CurseMod curseMod = mapper.readValue(file, CurseMod.class);
			CurseFileMetadata metadata = cache.resolveMetadata(curseMod);

			FileInstall entry = new FileInstall();
			String to = String.format("mods/%s", metadata.getFileName());

			entry.setVersion(metadata.getFileDate());
			entry.setLocation(metadata.getDownloadUrl());
			entry.setTo(to);
			entry.setSize(metadata.getFileLength());

			if (curseMod.getFeature() != null) {
				applicator.register(FileInfoScanner.fromPattern(to, curseMod.getFeature()));
			}

			log.info(String.format("Adding Curse mod %s", metadata.getDisplayName()));
			applicator.apply(entry);
			manifest.getTasks().add(entry);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected DirectoryBehavior getBehavior(String name) {
		if (name.startsWith(".")) {
			return DirectoryBehavior.SKIP;
		} else if (name.equals("_OPTIONAL")) {
			return DirectoryBehavior.IGNORE;
		} else if (name.equals("_SERVER")) {
			return DirectoryBehavior.SKIP;
		} else if (name.equals("_CLIENT")) {
			return DirectoryBehavior.IGNORE;
		} else {
			return DirectoryBehavior.CONTINUE;
		}
	}
}
