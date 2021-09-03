package com.skcraft.launcher.builder.loaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import com.google.common.io.Files;
import com.skcraft.launcher.builder.BuilderUtils;
import com.skcraft.launcher.model.loader.profiles.LegacyInstallProfile;
import com.skcraft.launcher.model.minecraft.GameArgument;
import com.skcraft.launcher.model.minecraft.Library;
import com.skcraft.launcher.model.minecraft.MinecraftArguments;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import com.skcraft.launcher.model.modpack.Manifest;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

@Log
public class OldForgeLoaderProcessor implements ILoaderProcessor {
	@Override
	public LoaderResult process(File loaderJar, Manifest manifest, ObjectMapper mapper, File baseDir) throws IOException {
		JarFile jarFile = new JarFile(loaderJar);
		LoaderResult result = new LoaderResult();
		Closer closer = Closer.create();

		try {
			ZipEntry profileEntry = BuilderUtils.getZipEntry(jarFile, "install_profile.json");

			if (profileEntry != null) {
				InputStream stream = jarFile.getInputStream(profileEntry);

				// Read file
				String data = BuilderUtils.readStringFromStream(closer.register(new InputStreamReader(stream)));
				LegacyInstallProfile profile = mapper.readValue(data, LegacyInstallProfile.class);
				VersionManifest version = manifest.getVersionManifest();

				// Copy tweak class arguments
				MinecraftArguments args = profile.getVersionInfo().getArguments();
				if (args != null) {
					Iterator<GameArgument> iter = args.getGameArguments().iterator();
					while (iter.hasNext()) {
						GameArgument cur = iter.next();
						if (cur.getValues().contains("--tweakClass")) {
							String tweakClass = cur.getValues().size() > 1
									? cur.getValues().get(1)
									: iter.next().getJoinedValue();

							List<GameArgument> gameArgs = manifest.getVersionManifest().getArguments().getGameArguments();
							gameArgs.add(new GameArgument("--tweakClass"));
							gameArgs.add(new GameArgument(tweakClass));

							log.info(String.format("Adding tweak class '%s' to arguments", tweakClass));
						}
					}
				}

				// Add libraries
				List<Library> libraries = profile.getVersionInfo().getLibraries();
				if (libraries != null) {
					for (Library library : libraries) {
						if (!version.getLibraries().contains(library)) {
							result.getLoaderLibraries().add(library);
						}
					}
				}

				// Copy main class
				String mainClass = profile.getVersionInfo().getMainClass();
				if (mainClass != null) {
					version.setMainClass(mainClass);
					log.info("Using " + mainClass + " as the main class");
				}

				// Extract the library
				String filePath = profile.getInstallData().getFilePath();
				String libraryPath = profile.getInstallData().getPath();

				if (filePath != null && libraryPath != null) {
					ZipEntry libraryEntry = BuilderUtils.getZipEntry(jarFile, filePath);

					if (libraryEntry != null) {
						File librariesDir = new File(baseDir, "libraries");
						File extractPath = new File(librariesDir, Library.mavenNameToPath(libraryPath));

						Files.createParentDirs(extractPath);
						ByteStreams.copy(closer.register(jarFile.getInputStream(libraryEntry)),
								Files.newOutputStreamSupplier(extractPath));
					} else {
						log.warning("Could not find the file '" + filePath + "' in "
								+ loaderJar.getAbsolutePath()
								+ ", which means that this mod loader will not work correctly");
					}
				}
			} else {
				log.warning("The file at " + loaderJar.getAbsolutePath() + " did not appear to have an " +
						"install_profile.json file inside -- is it actually an installer for a mod loader?");
			}
		} finally {
			closer.close();
			jarFile.close();
		}

		return result;
	}
}
