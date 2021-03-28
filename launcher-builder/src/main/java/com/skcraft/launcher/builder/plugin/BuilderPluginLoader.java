package com.skcraft.launcher.builder.plugin;

import com.google.common.collect.Lists;
import com.skcraft.launcher.builder.BuilderOptions;
import com.skcraft.launcher.model.modpack.Manifest;
import lombok.extern.java.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Log
public class BuilderPluginLoader {
	private static ClassLoader pluginClassLoader;
	private List<BuilderPlugin> loadedPlugins = Lists.newArrayList();

	public void loadClasses(List<String> classNames) {
		for (String pluginClassName : classNames) {
			try {
				BuilderPlugin plugin = loadClass(pluginClassName);

				if (plugin != null) {
					loadedPlugins.add(plugin);
				}
			} catch (ClassNotFoundException e) {
				log.severe("Failed to load plugin " + pluginClassName + ", is it on the classpath?");
			}
		}
	}

	private <T extends BuilderPlugin> T loadClass(String pluginClassName) throws ClassNotFoundException {
		try {
			Class<T> pluginClass = (Class<T>) getClassLoader().loadClass(pluginClassName);

			return pluginClass.getConstructor().newInstance();
		} catch (InstantiationException e) {
			log.warning("Plugin class " + pluginClassName + " could not be created!");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			log.warning("Plugin class " + pluginClassName + " could not be accessed!");
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			log.warning("Plugin class " + pluginClassName + " threw an error while initializing!");
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			log.warning("Plugin class " + pluginClassName + " is missing a primary constructor!");
		} catch (ClassCastException e) {
			log.warning("Plugin class" + pluginClassName + " does not extend BuilderPlugin!");
		}

		return null;
	}

	private static ClassLoader getClassLoader() {
		if (pluginClassLoader != null) return pluginClassLoader;

		return BuilderPluginLoader.class.getClassLoader();
	}

	public static void setClassLoader(ClassLoader loader) {
		pluginClassLoader = loader;
	}

	public void dispatchAcceptOptions(BuilderOptions options, String[] args) {
		for (BuilderPlugin plugin : loadedPlugins) {
			plugin.acceptOptions(options, args);
		}
	}

	public void dispatchManifestCreated(Manifest manifest) {
		for (BuilderPlugin plugin : loadedPlugins) {
			plugin.onManifestCreated(manifest);
		}
	}

	public void dispatchBuild(Builder builder) throws IOException, InterruptedException {
		for (BuilderPlugin plugin : loadedPlugins) {
			plugin.onBuild(builder);
		}
	}
}
