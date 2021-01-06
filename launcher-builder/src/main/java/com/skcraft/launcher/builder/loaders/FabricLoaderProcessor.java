package com.skcraft.launcher.builder.loaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Closer;
import com.skcraft.launcher.builder.BuilderUtils;
import com.skcraft.launcher.model.loader.FabricMod;
import com.skcraft.launcher.model.minecraft.Library;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import com.skcraft.launcher.model.modpack.Manifest;
import com.skcraft.launcher.util.HttpRequest;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
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
			String loaderVersion;

			ZipEntry modEntry = BuilderUtils.getZipEntry(jarFile, "fabric.mod.json");
			if (modEntry != null) {
				InputStreamReader reader = new InputStreamReader(jarFile.getInputStream(modEntry));
				FabricMod loaderMod = mapper.readValue(
						BuilderUtils.readStringFromStream(closer.register(reader)), FabricMod.class);

				loaderVersion = loaderMod.getVersion();
			} else {
				log.warning("Fabric loader has no 'fabric.mod.json' file, is it really a Fabric Loader jar?");
				return null;
			}

			log.info("Downloading fabric metadata...");
			URL metaUrl = HttpRequest.url(
					String.format("https://meta.fabricmc.net/v2/versions/loader/%s/%s/profile/json",
							manifest.getGameVersion(), loaderVersion));
			VersionManifest fabricManifest = HttpRequest.get(metaUrl)
					.execute()
					.expectResponseCode(200)
					.returnContent()
					.asJson(VersionManifest.class);

			for (Library library : fabricManifest.getLibraries()) {
				result.getLoaderLibraries().add(library);
				log.info("Adding loader library " + library.getName());
			}

			String mainClass = fabricManifest.getMainClass();
			if (mainClass != null) {
				manifest.getVersionManifest().setMainClass(mainClass);
				log.info("Using main class " + mainClass);
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
