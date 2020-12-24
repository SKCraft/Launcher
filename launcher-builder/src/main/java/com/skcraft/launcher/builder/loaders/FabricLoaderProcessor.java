package com.skcraft.launcher.builder.loaders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.common.io.Closer;
import com.skcraft.launcher.builder.BuilderUtils;
import com.skcraft.launcher.model.loader.MavenUrl;
import com.skcraft.launcher.model.loader.profiles.FabricInstallProfile;
import com.skcraft.launcher.model.minecraft.Library;
import com.skcraft.launcher.model.modpack.Manifest;
import com.skcraft.launcher.util.HttpRequest;
import lombok.extern.java.Log;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

@Log
public class FabricLoaderProcessor implements ILoaderProcessor {
	@Override
	public LoaderResult process(File loaderJar, Manifest manifest, ObjectMapper mapper, File baseDir) throws IOException {
		JarFile jarFile = new JarFile(loaderJar);
		LoaderResult result = new LoaderResult();
		Closer closer = Closer.create();

		try {
			ZipEntry installerEntry = BuilderUtils.getZipEntry(jarFile, "fabric-installer.json");

			if (installerEntry != null) {
				InputStreamReader reader = new InputStreamReader(jarFile.getInputStream(installerEntry));
				FabricInstallProfile profile = mapper.readValue(
						BuilderUtils.readStringFromStream(closer.register(reader)), FabricInstallProfile.class);

				// Check version
				if (profile.getVersion() != 1) {
					log.warning(String.format("Fabric installer metadata version is %d - we expect version 1.",
							profile.getVersion()));
				}

				// Add libraries (TODO: Add server-only libraries to somewhere)
				Iterable<Library> libraries = Iterables.concat(profile.getLibraries().getClient(),
						profile.getLibraries().getCommon());
				for (Library library : libraries) {
					result.getLoaderLibraries().add(library);
					log.info("Adding loader library " + library.getName());
				}

				// Add actual loader jar into library path
				if (profile.getLoader() != null) {
					result.getLoaderLibraries().add(profile.getLoader());
					log.info(String.format("Adding Fabric Loader '%s'", profile.getLoader().getName()));
				} else {
					log.warning("Fabric loader metadata is missing a `loader` section, making up a fake library");
					Library loader = new Library();
					loader.setName("faked:loader:" + FilenameUtils.getBaseName(loaderJar.getName()));

					Library.Downloads downloads = new Library.Downloads();
					downloads.setArtifact(new Library.Artifact());
					downloads.getArtifact().setPath(loaderJar.getName());
					downloads.getArtifact().setUrl("");
					loader.setDownloads(downloads);

					result.getLoaderLibraries().add(loader);
					// Little bit of a hack here, pretending the filesystem is a maven
					result.getJarMavens().add(new URL("file:" + loaderJar.getParentFile().getAbsolutePath() + "/"));
				}

				// Set main class
				String mainClass = profile.getMainClass().getClient();
				if (mainClass != null) {
					manifest.getVersionManifest().setMainClass(mainClass);
					log.info("Using main class " + mainClass);
				}

				// Add intermediary library
				log.info("Downloading fabric metadata...");
				URL url = HttpRequest.url("https://meta.fabricmc.net/v2/versions/intermediary/"
						+ manifest.getVersionManifest().getId());
				List<MavenUrl> versions = HttpRequest.get(url)
						.execute()
						.expectResponseCode(200)
						.returnContent()
						.asJson(new TypeReference<List<MavenUrl>>() {});

				if (versions != null && versions.size() > 0) {
					MavenUrl intermediaryLib = versions.get(0);

					if (intermediaryLib.getUrl() == null) {
						// FIXME temporary hack since maven URL is missing, hopefully can go away soon
						//  waiting on PR FabricMC/fabric-meta#9
						intermediaryLib.setUrl("https://maven.fabricmc.net/");
					}

					result.getLoaderLibraries().add(intermediaryLib.toLibrary());
					log.info("Added intermediary " + intermediaryLib.getName());
				}
			}
		} catch (InterruptedException e) {
			log.warning("HTTP request to fabric metadata API was interrupted, this will probably not work!");
		} finally {
			closer.close();
			jarFile.close();
		}

		return result;
	}
}
