/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.minecraft;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.skcraft.launcher.util.Environment;
import com.skcraft.launcher.util.Platform;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Library {

    private String name;
    private Downloads downloads;
    private Map<String, String> natives;
    private Extract extract;
    private List<Rule> rules;

    // Forge-added
    private String comment;

    // Custom
    private boolean locallyAvailable;

    public boolean matches(Environment environment) {
        boolean allow = false;

        if (getRules() != null) {
            for (Rule rule : getRules()) {
                if (rule.matches(environment, FeatureList.EMPTY)) {
                    allow = rule.isAllowed();
                }
            }
        } else {
            allow = true;
        }

        return allow;
    }

    public String getNativeString(Platform platform) {
        if (getNatives() != null) {
            switch (platform) {
                case LINUX:
                    return getNatives().get("linux");
                case WINDOWS:
                    return getNatives().get("windows");
                case MAC_OS_X:
                    return getNatives().get("osx");
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    public Artifact getArtifact(Environment environment) {
        String nativeString = getNativeString(environment.getPlatform());

        if (nativeString != null) {
            return getDownloads().getClassifiers().get(nativeString);
        } else {
            return getDownloads().getArtifact();
        }
    }

    public String getPath(Environment environment) {
        return getArtifact(environment).getPath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Library library = (Library) o;

        if (name != null ? !name.equals(library.name) : library.name != null)
            return false;

        // If libraries have different natives lists, they should be separate.
        if (natives != null ? !natives.equals(library.natives) : library.natives != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Data
    public static class Extract {
        private List<String> exclude;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Artifact {
        private String path;
        private String url;
        private String sha1;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Downloads {
        private Artifact artifact;
        private Map<String, Artifact> classifiers;
    }

    // Support for old Forge distributions with legacy library specs.
    public void setUrl(String url) {
        Artifact virtualArtifact = new Artifact();

        virtualArtifact.setUrl(url);
        virtualArtifact.setPath(mavenNameToPath(name));

        Downloads downloads = new Downloads();
        downloads.setArtifact(virtualArtifact);

        setDownloads(downloads);
    }

    public void setServerreq(boolean value) {
        if (value) {
            setUrl("https://libraries.minecraft.net/"); // TODO do something better than this
        }
    }

    public static String mavenNameToPath(String mavenName) {
        List<String> split = Splitter.on(':').splitToList(mavenName);
        int size = split.size();

        String group = split.get(0);
        String name = split.get(1);
        String version = split.get(2);
        String extension = "jar";

        String fileName = name + "-" + version;

        if (size > 3) {
            String classifier = split.get(3);

            if (classifier.indexOf("@") != -1) {
                List<String> parts = Splitter.on('@').splitToList(classifier);

                classifier = parts.get(0);
                extension = parts.get(1);
            }

            fileName += "-" + classifier;
        }

        fileName += "." + extension;

        return Joiner.on('/').join(group.replace('.', '/'), name, version, fileName);
    }
}
