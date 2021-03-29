package com.skcraft.plugin.curse.creator;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skcraft.launcher.builder.DirectoryWalker;
import com.skcraft.plugin.curse.CurseApi;
import com.skcraft.plugin.curse.CurseMod;
import com.skcraft.plugin.curse.model.CurseProject;
import com.skcraft.plugin.curse.model.LoadedMod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class PackModScanner extends DirectoryWalker {
	private final ObjectMapper mapper;

	@Getter private final List<LoadedMod> result = Lists.newArrayList();

	@Override
	@SneakyThrows
	protected void onFile(File file, String relPath) throws IOException {
		CurseMod mod = mapper.readValue(file, CurseMod.class);
		CurseProject project = CurseApi.getById(mod.getProjectId());

		result.add(new LoadedMod(mod, project));
	}
}
