package com.skcraft.launcher.model.loader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstallProcessor {
	private String jar;
	private List<String> classpath;
	private List<String> args;
	private Map<String, String> outputs;

	public List<String> resolveArgs(LoaderSubResolver resolver) {
		return Lists.transform(getArgs(), resolver);
	}

	public Map<String, String> resolveOutputs(final LoaderSubResolver resolver) {
		if (getOutputs() == null) return Collections.emptyMap();

		HashMap<String, String> result = new HashMap<String, String>();

		for (Map.Entry<String, String> entry : getOutputs().entrySet()) {
			result.put(resolver.apply(entry.getKey()), resolver.apply(entry.getValue()));
		}

		return result;
	}
}
