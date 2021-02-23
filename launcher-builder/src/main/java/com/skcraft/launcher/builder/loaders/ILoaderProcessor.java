package com.skcraft.launcher.builder.loaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skcraft.launcher.model.modpack.Manifest;

import java.io.File;
import java.io.IOException;

public interface ILoaderProcessor {
	LoaderResult process(File loaderJar, Manifest manifest, ObjectMapper mapper, File baseDir) throws IOException;
}
