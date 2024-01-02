/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.minecraft;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.skcraft.launcher.util.Environment;
import lombok.Data;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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
    @JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
    private boolean generated;

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

            if (nativeString != null) {
                return nativeString.replace("${arch}", environment.getArchBits());
            }
        }

        return null;
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

        EqualsBuilder builder = new EqualsBuilder();
        builder.append(name, library.getName());
        // If libraries have different natives lists, they should be separate.
        builder.append(natives, library.getNatives());

        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(45, 23);

        if (name != null)
            builder.append(name);

        if (natives != null)
            builder.append(natives);

        return builder.toHashCode();
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
        if (getName() != null) {
            virtualArtifact.setPath(mavenNameToPath(getName()));
        }

        Downloads downloads = new Downloads();
        downloads.setArtifact(virtualArtifact);

        setDownloads(downloads);
    }

    public void setName(String name) {
        int classifierPos = name.indexOf("@");
        if (classifierPos != -1) {
            // Take off classifiers
            name = name.substring(0, classifierPos);
        }

        this.name = name;

        // [DEEP SIGH]
        // Sometimes 'name' comes after 'url', and I can't figure out how to get Jackson to enforce order
        // So we have to do this silly check to make sure we have a path.
        if (getDownloads() != null) {
            if (getDownloads().getArtifact() == null) return;

            if (getDownloads().getArtifact().getPath() == null) {
                getDownloads().getArtifact().setPath(mavenNameToPath(name));
            }
        }
    }

    /**
     * BACKWARDS COMPATIBILITY:
     * Some old Forge distributions use a parameter called "serverreq" to indicate that the dependency should
     * be fetched from the Minecraft library source; this setter handles that.
     */
    public void setServerreq(boolean value) {
        if (value && getDownloads() == null) {
            setUrl("https://libraries.minecraft.net/"); // TODO get this from properties?
        }
    }

    /**
     * Classifier-independent library name check
     * @param mavenName Maven name of a library, possibly with a classifier
     * @return True if this library is named 'mavenName'.
     */
    public boolean matches(String mavenName) {
        int classifierPos = mavenName.indexOf('@');
        if (classifierPos != -1) {
            mavenName = mavenName.substring(0, classifierPos);
        }

        return this.name.equals(mavenName);
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
