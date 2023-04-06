package com.skcraft.launcher.builder.loaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Closer;
import com.skcraft.launcher.builder.BuilderUtils;
import com.skcraft.launcher.model.loader.FabricMod;
import com.skcraft.launcher.model.loader.QuiltMod;
import com.skcraft.launcher.model.loader.Versionable;
import com.skcraft.launcher.model.minecraft.Library;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import com.skcraft.launcher.model.modpack.Manifest;
import com.skcraft.launcher.util.HttpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

@Log
@RequiredArgsConstructor
public class FabricLoaderProcessor implements ILoaderProcessor {
	private final Variant variant;

	@Override
	public LoaderResult process(File loaderJar, Manifest manifest, ObjectMapper mapper, File baseDir) throws IOException {
		JarFile jarFile = new JarFile(loaderJar);
		LoaderResult result = new LoaderResult();
		Closer closer = Closer.create();

		try {
			Versionable loaderMod;

			ZipEntry modEntry = BuilderUtils.getZipEntry(jarFile, variant.modJsonName);
			if (modEntry != null) {
				InputStreamReader reader = new InputStreamReader(jarFile.getInputStream(modEntry));

				loaderMod = mapper.readValue(
						BuilderUtils.readStringFromStream(closer.register(reader)), variant.mappedClass);
			} else {
				log.warning(String.format("%s loader has no '%s' file, is it really a %s Loader jar?",
						variant.friendlyName, variant.modJsonName, variant.friendlyName));
				return null;
			}

			log.info(String.format("Downloading %s metadata...", variant.friendlyName));
			URL metaUrl = HttpRequest.url(
					String.format(variant.metaUrl, manifest.getGameVersion(), loaderMod.getVersion()));
			VersionManifest fabricManifest = HttpRequest.get(metaUrl)
					.execute()
					.expectResponseCode(200)
					.returnContent()
					.asJson(VersionManifest.class);

			for (Library library : fabricManifest.getLibraries()) {
				// To quote a famous comment: "And here we come upon a sad state of affairs."
				// Quilt's meta API returns broken data about how to launch the game. It specifies its own incomplete
				// and ultimately broken set of intermediary mappings called "hashed". If the loader finds "hashed" in
				// the classpath it tries to use it and blows up because it doesn't work.
				// We work around this here by just throwing the hashed library out - they do now at least specify
				// fabric's intermediary mappings in the library list, which DO work.
				// Historical note: previously they didn't do this! Every launcher that added Quilt support had to add
				// a hack that replaced hashed with intermediary! This is a lot of technical debt that is gonna come
				// back to bite them in the ass later, because it's all still there!
				// TODO pester Quilt again about fixing this....
				if (library.getName().startsWith("org.quiltmc:hashed") && loaderMod instanceof QuiltMod) {
					continue;
				}

				result.getLoaderLibraries().add(library);
				log.info("Adding loader library " + library.getName());
			}

			String mainClass = fabricManifest.getMainClass();
			if (mainClass != null) {
				manifest.getVersionManifest().setMainClass(mainClass);
				log.info("Using main class " + mainClass);
			}
		} catch (InterruptedException e) {
			log.warning(String.format("HTTP request to %s metadata API was interrupted!", variant.friendlyName));
			throw new IOException(e);
		} finally {
			closer.close();
			jarFile.close();
		}

		return result;
	}

	@RequiredArgsConstructor
	public enum Variant {
		FABRIC("Fabric", "fabric.mod.json", "https://meta.fabricmc.net/v2/versions/loader/%s/%s/profile/json", FabricMod.class),
		QUILT("Quilt", "quilt.mod.json", "https://meta.quiltmc.org/v3/versions/loader/%s/%s/profile/json", QuiltMod.class);

		private final String friendlyName;
		private final String modJsonName;
		private final String metaUrl;
		private final Class<? extends Versionable> mappedClass;
	}
}
