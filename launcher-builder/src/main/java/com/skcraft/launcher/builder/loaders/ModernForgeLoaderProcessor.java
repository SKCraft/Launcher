package com.skcraft.launcher.builder.loaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.Closer;
import com.skcraft.launcher.builder.BuilderUtils;
import com.skcraft.launcher.model.loader.LoaderManifest;
import com.skcraft.launcher.model.loader.SidedData;
import com.skcraft.launcher.model.loader.VersionInfo;
import com.skcraft.launcher.model.loader.profiles.ModernForgeInstallProfile;
import com.skcraft.launcher.model.minecraft.GameArgument;
import com.skcraft.launcher.model.minecraft.Library;
import com.skcraft.launcher.model.minecraft.Side;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import com.skcraft.launcher.model.modpack.DownloadableFile;
import com.skcraft.launcher.model.modpack.Manifest;
import com.skcraft.launcher.util.FileUtils;
import lombok.extern.java.Log;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

@Log
public class ModernForgeLoaderProcessor implements ILoaderProcessor {
	@Override
	public LoaderResult process(File loaderJar, Manifest manifest, ObjectMapper mapper, File baseDir) throws IOException {
		JarFile jarFile = new JarFile(loaderJar);
		Closer closer = Closer.create();
		LoaderResult result = new LoaderResult();

		try {
			ZipEntry versionEntry = BuilderUtils.getZipEntry(jarFile, "version.json");
			String loaderName = jarFile.getName();

			if (versionEntry != null) {
				InputStream stream = jarFile.getInputStream(versionEntry);

				VersionInfo info = mapper.readValue(
						BuilderUtils.readStringFromStream(closer.register(new InputStreamReader(stream))),
						VersionInfo.class);
				VersionManifest version = manifest.getVersionManifest();

				if (info.getId() != null) {
					loaderName = info.getId();
				}

				// Copy game arguments
				List<GameArgument> gameArguments = info.getArguments().getGameArguments();
				if (gameArguments != null) {
					if (info.isOverridingArguments()) {
						version.getArguments().getGameArguments().clear();
					}

					version.getArguments().getGameArguments().addAll(gameArguments);
				}

				// Copy JVM arguments
				List<GameArgument> jvmArguments = info.getArguments().getJvmArguments();
				if (jvmArguments != null) {
					version.getArguments().getJvmArguments().addAll(jvmArguments);
				}

				// Add libraries
				List<Library> libraries = info.getLibraries();
				if (libraries != null) {
					for (Library library : libraries) {
						result.getLoaderLibraries().add(library);
						log.info("Adding loader library " + library.getName());
					}
				}

				// Copy logging config
				SidedData<VersionManifest.LoggingConfig> loggingConfig = info.getLogging();
				if (loggingConfig != null) {
					version.setLogging(loggingConfig);
				}

				// Copy main class
				String mainClass = info.getMainClass();
				if (mainClass != null) {
					version.setMainClass(mainClass);
					log.info("Using " + mainClass + " as the main class");
				}
			} else {
				log.warning("The loader " + loaderJar.getAbsolutePath() + " does not appear to have a " +
						"version.json file inside -- is it actually an installer for Forge?");
			}

			ZipEntry profileEntry = BuilderUtils.getZipEntry(jarFile, "install_profile.json");
			if (profileEntry != null) {
				InputStream stream = jarFile.getInputStream(profileEntry);
				String data = CharStreams.toString(closer.register(new InputStreamReader(stream)));
				data = data.replace(",\\s*\\}", "}");

				ModernForgeInstallProfile profile = mapper.readValue(data, ModernForgeInstallProfile.class);

				if (!profile.getMinecraft().equals(manifest.getGameVersion())) {
					// TODO: Ideally this would show up as a Problem when running the check command.
					//  Getting the data from here to there is quite difficult, however.
					log.warning(String.format("The Forge installer inside loaders/ is for Minecraft version %s; your " +
							"manifest is set to %s.", profile.getMinecraft(), manifest.getGameVersion()));
				}

				// Import the libraries for the installer
				result.getProcessorLibraries().addAll(profile.getLibraries());

				// Extract the data files
				List<DownloadableFile> extraFiles = Lists.newArrayList();
				File objectsDir = new File(baseDir, manifest.getObjectsLocation());

				ZipEntry clientBinpatch = BuilderUtils.getZipEntry(jarFile, "data/client.lzma");
				if (clientBinpatch != null) {
					DownloadableFile entry = FileUtils.saveStreamToObjectsDir(
							closer.register(jarFile.getInputStream(clientBinpatch)),
							objectsDir);

					entry.setName("client.lzma");
					entry.setSide(Side.CLIENT);
					extraFiles.add(entry);
					profile.getData().get("BINPATCH").setClient("&" + entry.getName() + "&");
				}

				ZipEntry serverBinpatch = BuilderUtils.getZipEntry(jarFile, "data/server.lzma");
				if (serverBinpatch != null) {
					DownloadableFile entry = FileUtils.saveStreamToObjectsDir(
							closer.register(jarFile.getInputStream(serverBinpatch)),
							objectsDir);

					entry.setName("server.lzma");
					entry.setSide(Side.SERVER);
					extraFiles.add(entry);
					profile.getData().get("BINPATCH").setServer("&" + entry.getName() + "&");
				}

				// Forge install profile spec version 1 and above.
				if (profile.getSpec() >= 1) {
					// Add the installer itself to the extra files.
					// This is for a server-only task like above, but hey.
					DownloadableFile entry = FileUtils.saveStreamToObjectsDir(
							closer.register(new FileInputStream(loaderJar)), objectsDir);

					entry.setName(loaderJar.getName());
					entry.setSide(Side.SERVER);
					extraFiles.add(entry);

					profile.getData().put("INSTALLER", SidedData.of("&" + entry.getName() + "&"));
				}

				// Add extra sided data
				profile.getData().put("SIDE", SidedData.create("client", "server"));

				// Add loader manifest to the map
				manifest.getLoaders().put(loaderName, new LoaderManifest(profile.getLibraries(), profile.getData(), extraFiles));

				// Find name of final patched library and mark it as excluded from download
				// TODO: we should generalize this to all process outputs, really
				SidedData<String> finalJars = profile.getData().get("PATCHED");
				if (finalJars != null) {
					String libraryName = finalJars.getClient();
					libraryName = libraryName.substring(1, libraryName.length() - 1);

					for (Library lib : result.getLoaderLibraries()) {
						if (lib.matches(libraryName)) {
							lib.setGenerated(true);
							log.info(String.format("Setting generated flag on library '%s'", lib.getName()));
							break;
						}
					}
				}

				// Add processors
				manifest.getTasks().addAll(profile.toProcessorEntries(loaderName));
			}

			ZipEntry mavenEntry = BuilderUtils.getZipEntry(jarFile, "maven/");
			if (mavenEntry != null) {
				URL jarUrl = new URL("jar:file:" + loaderJar.getAbsolutePath() + "!/");
				result.getJarMavens().add(new URL(jarUrl, "/maven/"));
			}
		} finally {
			closer.close();
			jarFile.close();
		}

		return result;
	}
}
