/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.minecraft;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.skcraft.launcher.util.Environment;
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

    public String getNativeString(Environment environment) {
        if (getNatives() != null) {
            String nativeString;

            switch (environment.getPlatform()) {
                case LINUX:
                    nativeString = getNatives().get("linux");
                    break;
                case WINDOWS:
                    nativeString = getNatives().get("windows");
                    break;
                case MAC_OS_X:
                    nativeString = getNatives().get("osx");
                    break;
                default:
                    return null;
            }

            return nativeString.replace("${arch}", environment.getArchBits());
        } else {
            return null;
        }
    }

    public void ensureDownloadsExist() {
        if (getDownloads() == null) {
            setServerreq(true); // BACKWARDS COMPATIBILITY
        }
    }

    /**
     * BACKWARDS COMPATIBILITY:
     * Some library definitions only come with a "name" key and don't trigger any other compatibility measures.
     * Therefore, if a library has no artifacts when this is called, we call {@link #setServerreq)} to trigger
     * artifact generation that assumes the source is the Minecraft libraries URL.
     * There is also some special handling for natives in this function; if we have no extra artifacts (newer specs
     * put this in "classifiers" in the download list) then we make up an artifact by adding a maven classifier to
     * the library name and using that.
     */
    public Artifact getArtifact(Environment environment) {
        ensureDownloadsExist();

        String nativeString = getNativeString(environment);

        if (nativeString != null) {
            if (getDownloads().getClassifiers() == null) {
                // BACKWARDS COMPATIBILITY: make up a virtual artifact
                Artifact virtualArtifact = new Artifact();
                virtualArtifact.setUrl(getDownloads().getArtifact().getUrl());
                virtualArtifact.setPath(mavenNameToPath(name + ":" + nativeString));

                return virtualArtifact;
            }

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
        private int size;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Downloads {
        private Artifact artifact;
        private Map<String, Artifact> classifiers;

        @JsonIgnore
        public List<Artifact> getAllArtifacts() {
            List<Artifact> artifacts = Lists.newArrayList();

            if (artifact != null)
                artifacts.add(artifact);

            if (classifiers != null)
                artifacts.addAll(classifiers.values());

            return artifacts;
        }
    }

    /**
     * BACKWARDS COMPATIBILITY:
     * Various sources use the old-style library specification, where there are two keys - "name" and "url",
     * rather than the newer multiple-artifact style. This setter is called by Jackson when the "url" property
     * is present, and uses it to create a "virtual" artifact using the URL given to us here plus the library
     * name parsed out into a path.
     */
    public void setUrl(String url) {
        Artifact virtualArtifact = new Artifact();

        virtualArtifact.setUrl(url);
        virtualArtifact.setPath(mavenNameToPath(name));

        Downloads downloads = new Downloads();
        downloads.setArtifact(virtualArtifact);

        setDownloads(downloads);
    }

    /**
     * BACKWARDS COMPATIBILITY:
     * Some old Forge distributions use a parameter called "serverreq" to indicate that the dependency should
     * be fetched from the Minecraft library source; this setter handles that.
     */
    public void setServerreq(boolean value) {
        if (value) {
            setUrl("https://libraries.minecraft.net/"); // TODO get this from properties?
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
