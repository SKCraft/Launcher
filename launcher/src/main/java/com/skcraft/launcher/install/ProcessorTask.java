package com.skcraft.launcher.install;

import com.google.common.collect.Lists;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.model.loader.InstallProcessor;
import com.skcraft.launcher.model.loader.LoaderManifest;
import com.skcraft.launcher.model.loader.LoaderSubResolver;
import com.skcraft.launcher.model.loader.SidedData;
import com.skcraft.launcher.model.minecraft.Library;
import com.skcraft.launcher.model.minecraft.Side;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import com.skcraft.launcher.model.modpack.DownloadableFile;
import com.skcraft.launcher.model.modpack.Manifest;
import com.skcraft.launcher.util.Environment;
import com.skcraft.launcher.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import static com.skcraft.launcher.util.SharedLocale.tr;

@RequiredArgsConstructor
@Log
public class ProcessorTask implements InstallTask {
	private final InstallProcessor processor;
	private final LoaderManifest loaderManifest;
	private final Manifest manifest;
	private final HashMap<String, DownloadableFile.LocalFile> localFiles;

	private transient String message = "";
	private transient double progress = 0;

	@Override
	public void execute(Launcher launcher) throws Exception {
		VersionManifest versionManifest = manifest.getVersionManifest();

		LoaderSubResolver resolver = new LoaderSubResolver(manifest, loaderManifest,
				Environment.getInstance(), Side.CLIENT, launcher.getLibrariesDir(), localFiles);

		Map<String, SidedData<String>> sidedData = loaderManifest.getSidedData();
		sidedData.put("ROOT", SidedData.of(launcher.getInstallerDir().getAbsolutePath()));
		sidedData.put("MINECRAFT_JAR", SidedData.of(launcher.getJarPath(versionManifest).getAbsolutePath()));
		sidedData.put("LIBRARY_DIR", SidedData.of(launcher.getLibrariesDir().getAbsolutePath()));
		sidedData.put("MINECRAFT_VERSION", SidedData.of(versionManifest.getId()));

		message = "Resolving parameters";
		List<String> programArgs = processor.resolveArgs(resolver);
		Map<String, String> outputs = processor.resolveOutputs(resolver);

		message = "Finding libraries";
		Library execFile = loaderManifest.findLibrary(processor.getJar());
		File jar = launcher.getLibraryFile(execFile);

		JarFile jarFile = new JarFile(jar);
		String mainClass = jarFile.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
		jarFile.close();

		if (mainClass == null || mainClass.isEmpty()) {
			throw new RuntimeException(String.format("Processor jar file '%s' has no main class!", processor.getJar()));
		}

		List<URL> classpath = Lists.newArrayList(jar.toURI().toURL());
		int i = 0;
		int total = processor.getClasspath().size();
		for (String libraryName : processor.getClasspath()) {
			message = "Adding library " + libraryName;
			File libraryFile = launcher.getLibraryFile(loaderManifest.findLibrary(libraryName));
			if (!libraryFile.exists()) {
				throw new RuntimeException(String.format("Missing library '%s' for processor '%s'",
						libraryName, processor.getJar()));
			}

			classpath.add(libraryFile.toURI().toURL());
			i++;
			progress = (double) i / total;
		}

		progress = 0.0;
		message = "Executing";

		log.info(String.format("Running processor '%s' with %d args", processor.getJar(), programArgs.size()));
		log.info("Arguments: [" + String.join(", ", programArgs) + "]");

		ClassLoader parent;
		try {
			// in java 9+ we need the platform classloader for access to certain modules
			parent = (ClassLoader) ClassLoader.class.getDeclaredMethod("getPlatformClassLoader")
					.invoke(null);
		} catch (Throwable ignored) {
			// java 8 or below it's a-ok to have no delegate
			parent = null;
		}

		ClassLoader prev = Thread.currentThread().getContextClassLoader();
		ClassLoader cl = new URLClassLoader(classpath.toArray(new URL[0]), parent);
		try {
			Class<?> mainClazz = Class.forName(mainClass, true, cl);
			Method main = mainClazz.getDeclaredMethod("main", String[].class);

			// engage spicy mode
			Thread.currentThread().setContextClassLoader(cl);
			main.invoke(null, (Object) programArgs.toArray(new String[0]));
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(prev);
		}

		message = "Verifying";
		progress = 1.0;

		if (!outputs.isEmpty()) {
			progress = 0.0;
			i = 0;
			total = outputs.size();
			for (Map.Entry<String, String> output : outputs.entrySet()) {
				File artifact = new File(output.getKey());

				if (!artifact.exists()) {
					throw new RuntimeException(String.format("Artifact '%s' missing", output.getKey()));
				}

				if (!FileUtils.getShaHash(artifact).equals(output.getValue())) {
					log.warning("Invalid hash, expected " + output.getValue());
					throw new RuntimeException(String.format("Artifact '%s' has invalid hash!", output.getKey()));
				}

				i++;
				progress = (double) i / total;
			}
		}
	}

	@Override
	public double getProgress() {
		return progress;
	}

	@Override
	public String getStatus() {
		return tr("installer.runningProcessor", processor.getJar(), message);
	}
}
