/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.skcraft.launcher.model.modpack.LaunchModifier;
import com.skcraft.launcher.model.modpack.Manifest;
import lombok.Data;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

@Data
public class BuilderConfig {

    private String name;
    private String title;
    private String gameVersion;
    @JsonProperty("launch")
    private LaunchModifier launchModifier = new LaunchModifier();
    private List<FeaturePattern> features = Lists.newArrayList();
    private FnPatternList userFiles = new FnPatternList();

    public void setLaunchModifier(LaunchModifier launchModifier) {
        this.launchModifier = launchModifier != null ? launchModifier : new LaunchModifier();
    }

    public void setFeatures(List<FeaturePattern> features) {
        this.features = features != null ? features : Lists.<FeaturePattern>newArrayList();
    }

    public void setUserFiles(FnPatternList userFiles) {
        this.userFiles = userFiles != null ? userFiles : new FnPatternList();
    }

    public void update(Manifest manifest) {
        manifest.updateName(getName());
        manifest.updateTitle(getTitle());
        manifest.updateGameVersion(getGameVersion());
        manifest.setLaunchModifier(getLaunchModifier());
    }

    public void registerProperties(PropertiesApplicator applicator) {
        if (features != null) {
            for (FeaturePattern feature : features) {
                checkNotNull(emptyToNull(feature.getFeature().getName()),
                        "Empty feature name found");
                applicator.register(feature);
            }
        }

        applicator.setUserFiles(userFiles);
    }

}
