/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.builder;

import com.skcraft.launcher.model.modpack.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PropertiesApplicator {

    private final Manifest manifest;
    private final Set<Feature> used = new HashSet<Feature>();
    private final List<FeaturePattern> features = new ArrayList<FeaturePattern>();
    @Getter @Setter
    private FnPatternList userFiles;

    public PropertiesApplicator(Manifest manifest) {
        this.manifest = manifest;
    }

    public void apply(ManifestEntry entry) {
        if (entry instanceof FileInstall) {
            apply((FileInstall) entry);
        }
    }

    private void apply(FileInstall entry) {
        String path = entry.getTargetPath();
        entry.setWhen(fromFeature(path));
        entry.setUserFile(isUserFile(path));
    }

    public boolean isUserFile(String path) {
        if (userFiles != null) {
            return userFiles.matches(path);
        } else {
            return false;
        }
    }

    public Condition fromFeature(String path) {
        List<Feature> found = new ArrayList<Feature>();
        for (FeaturePattern pattern : features) {
            if (pattern.matches(path)) {
                used.add(pattern.getFeature());
                found.add(pattern.getFeature());
            }
        }

        if (!found.isEmpty()) {
            return new RequireAny(found);
        } else {
            return null;
        }
    }

    public void register(FeaturePattern component) {
        features.add(component);
    }

    public List<Feature> getFeaturesInUse() {
        return new ArrayList<Feature>(used);
    }

}
