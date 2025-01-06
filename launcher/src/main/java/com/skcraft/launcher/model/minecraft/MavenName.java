package com.skcraft.launcher.model.minecraft;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(of = {"group", "path", "classifier"})
public class MavenName {
	@JsonValue
	private final String fullName;
	private final String group;
	private final String path;
	private final String version;
	private final String classifier;
	private final String extension;

	private final String filePath;

	@Override
	public String toString() {
		return fullName;
	}

	@JsonCreator
	public static MavenName from(String mavenName) {
		if (mavenName == null) return null;

		List<String> split = Splitter.on(':').splitToList(mavenName);
		int size = split.size();

		String group = split.get(0);
		String name = split.get(1);
		String version = split.get(2);
		String classifier = null;
		String extension = "jar";

		String fileName = name + "-" + version;

		if (size > 3) {
			classifier = split.get(3);

			if (classifier.indexOf("@") != -1) {
				List<String> parts = Splitter.on('@').splitToList(classifier);

				classifier = parts.get(0);
				extension = parts.get(1);
			}

			fileName += "-" + classifier;
		}

		fileName += "." + extension;
		String filePath = Joiner.on('/').join(group.replace('.', '/'), name, version, fileName);

		return new MavenName(mavenName, group, name, version, classifier, extension, filePath);
	}
}
