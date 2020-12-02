package com.skcraft.launcher.model.minecraft;

import com.google.common.collect.Maps;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * List of enabled features for Minecraft feature rules.
 */
@NoArgsConstructor
public class FeatureList {
	protected Map<String, Boolean> features = Maps.newHashMap();

	public boolean doesMatch(Map<String, Boolean> features) {
		for (Map.Entry<String, Boolean> entry : features.entrySet()) {
			if (!entry.getValue().equals(this.features.get(entry.getKey()))) {
				return false;
			}
		}

		return true;
	}

	public boolean hasFeature(String key) {
		return features.get(key) != null && features.get(key);
	}

	public static class Mutable extends FeatureList {
		public void addFeature(String key, boolean value) {
			features.put(key, value);
		}
	}

	public static final FeatureList EMPTY = new FeatureList();
}
