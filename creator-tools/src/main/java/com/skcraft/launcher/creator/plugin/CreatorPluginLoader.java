package com.skcraft.launcher.creator.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skcraft.launcher.builder.BuilderUtils;
import com.skcraft.launcher.builder.DirectoryWalker;
import com.skcraft.launcher.builder.plugin.BuilderPluginLoader;
import lombok.Data;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

@Log
public class CreatorPluginLoader extends DirectoryWalker {
	private static final ObjectMapper mapper = new ObjectMapper();
	private List<PluginCandidate> candidates = new ArrayList<>();

	@Override
	protected void onFile(File file, String relPath) throws IOException {
		JarFile jarFile;
		try {
			jarFile = new JarFile(file);
		} catch (ZipException e) {
			log.warning(String.format("Found a non-JAR file %s in plugins directory", file));
			return;
		}

		ZipEntry metaEntry = jarFile.getEntry("skcraftcreator.plugin.json");
		if (metaEntry != null) {
			InputStreamReader reader = new InputStreamReader(jarFile.getInputStream(metaEntry));
			CreatorPluginInfo pluginInfo = mapper.readValue(BuilderUtils.readStringFromStream(reader),
					CreatorPluginInfo.class);

			log.info("Found plugin " + pluginInfo.getId());
			candidates.add(new PluginCandidate(pluginInfo, file.toURI().toURL()));
		} else {
			log.warning(String.format("Found a non-plugin JAR file: %s", file));
		}
	}

	public List<CreatorPluginWrapper<?>> loadAll() {
		URLClassLoader pluginClassLoader = new URLClassLoader(
				candidates.stream().map(PluginCandidate::getJarUrl).toArray(URL[]::new),
				this.getClass().getClassLoader()
		);

		// Fun hack to make sure the builder can load plugins
		BuilderPluginLoader.setClassLoader(pluginClassLoader);

		return candidates.stream()
				.map(candidate -> loadPlugin(pluginClassLoader, candidate))
				.collect(Collectors.toList());
	}

	private <T extends CreatorToolsPlugin> CreatorPluginWrapper<T> loadPlugin(URLClassLoader classLoader, PluginCandidate candidate) {
		try {
			Class<T> pluginClass = (Class<T>) classLoader.loadClass(candidate.getInfo().getPluginClass());

			T instance = pluginClass.getConstructor().newInstance();
			return new CreatorPluginWrapper<>(candidate.getInfo(), instance);
		} catch (ClassNotFoundException e) {
			log.warning(candidate.format("Could not find plugin class %s for plugin %s"));
		} catch (ClassCastException e) {
			log.warning(candidate.format("Plugin main class %s (from plugin '%s') does not extend CreatorToolsPlugin!"));
		} catch (NoSuchMethodException e) {
			log.warning(candidate.format("Could not find constructor for class %s (of plugin '%s')!"));
		} catch (InstantiationException e) {
			log.warning(candidate.format("Could not instantiate class %s (from plugin '%s')!"));
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			log.warning(candidate.format("Error while initializing main class %s (from plugin '%s')!"));
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			log.warning(candidate.format("Could not access constructor for class %s (of plugin '%s')!"));
			e.printStackTrace();
		}

		return null;
	}

	@Data
	static class PluginCandidate {
		private final CreatorPluginInfo info;
		private final URL jarUrl;

		public String format(String format) {
			return String.format(format, info.getPluginClass(), info.getId());
		}
	}
}
