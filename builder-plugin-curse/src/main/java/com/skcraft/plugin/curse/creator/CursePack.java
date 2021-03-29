package com.skcraft.plugin.curse.creator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.concurrency.SettableProgress;
import com.skcraft.launcher.creator.model.creator.Pack;
import com.skcraft.plugin.curse.model.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

@Log
public class CursePack {
	@Getter private final CurseSearchResults searchResults = new CurseSearchResults();
	@Getter private final AddedModList modList = new AddedModList();
	private final ObjectMapper mapper;

	private final String gameVersion;
	private final File curseModsDir;

	public CursePack(ObjectMapper mapper, Pack pack) {
		this.mapper = mapper;
		this.gameVersion = pack.getCachedConfig().getGameVersion();
		this.curseModsDir = new File(pack.getDirectory(), "cursemods");
	}

	public void addMod(CurseProject project) throws IOException, MissingVersionException {
		GameVersionFile forVersion = project.findFileForVersion(gameVersion);

		if (forVersion == null) {
			throw new MissingVersionException("Mod %s isn't available for this version.");
		}

		AddedMod loadedMod = project.toLoadedMod(forVersion);
		modList.add(loadedMod);

		File target = loadedMod.getDiskLocation(curseModsDir);
		log.info(String.format("Saving mod %s", target.getName()));
		mapper.writeValue(target, loadedMod.getMod());
	}

	public void removeMod(AddedMod mod) throws IOException {
		modList.remove(mod);

		File target = mod.getDiskLocation(curseModsDir);
		log.info(String.format("Removing mod %s", target.getName()));
		if (!target.delete()) {
			throw new IOException(String.format("Failed to delete %s", target));
		}
	}

	public AddModsCall addMany(List<CurseProject> projects) {
		return new AddModsCall(projects);
	}

	public RemoveModsCall removeMany(List<AddedMod> mods) {
		return new RemoveModsCall(mods);
	}

	@RequiredArgsConstructor
	private class AddModsCall implements Callable<Object>, ProgressObservable {
		private final List<CurseProject> projects;
		private final SettableProgress progress = new SettableProgress("", -1);

		@Override
		public Object call() throws Exception {
			int current = 0;
			int total = projects.size();

			for (CurseProject project : projects) {
				progress.set(String.format("Adding mod %s", project.getName()), 100 * (double) current / total);

				addMod(project);
				current++;
			}

			return null;
		}

		@Override
		public double getProgress() {
			return progress.getProgress();
		}

		@Override
		public String getStatus() {
			return progress.getStatus();
		}
	}

	@RequiredArgsConstructor
	private class RemoveModsCall implements Callable<Object>, ProgressObservable {
		private final List<AddedMod> mods;
		private final SettableProgress progress = new SettableProgress("", -1);

		@Override
		public Object call() throws Exception {
			int current = 0;
			int total = mods.size();

			for (AddedMod mod : mods) {
				progress.set(String.format("Removing mod %s", mod.getProject().getName()),
						100 * (double) current / total);

				removeMod(mod);
				current++;
			}

			return null;
		}

		@Override
		public double getProgress() {
			return progress.getProgress();
		}

		@Override
		public String getStatus() {
			return progress.getStatus();
		}
	}
}
