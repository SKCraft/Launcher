/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.minecraft;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.skcraft.launcher.util.Environment;
import com.skcraft.launcher.util.Platform;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Library {

    private String name;
    private String path;
    private transient String group;
    private transient String artifact;
    private transient String version;
    @JsonProperty("url")
    private String baseUrl;
    private Downloads downloads;
    private HashMap<String, String> natives;
    private Extract extract;
    private List<Rule> rules;

    // Forge-added
    private String comment;

    // Custom
    private boolean locallyAvailable;

    public void setName(String name) {
        this.name = name;

        if (name != null) {
            String[] parts = name.split(":");
            this.group = parts[0];
            this.artifact = parts[1];
            this.version = parts[2];
        } else {
            this.group = null;
            this.artifact = null;
            this.version = null;
        }
    }

    public boolean matches(Environment environment) {
        boolean allow = false;

        if (getRules() != null) {
            for (Rule rule : getRules()) {
                if (rule.matches(environment)) {
                    allow = rule.getAction() == Action.ALLOW;
                }
            }
        } else {
            allow = true;
        }

        return allow;
    }

    @JsonIgnore
    public String getGroup() {
        return group;
    }

    @JsonIgnore
    public String getArtifact() {
        return artifact;
    }

    @JsonIgnore
    public String getVersion() {
        return version;
    }

    public String getNativeString(Environment env) {
        if (getNatives() != null) {
            switch (env.getPlatform()) {
                case LINUX:
                    return getNatives().get("linux") == null ? null : getNatives().get("linux").replace("${arch}", env.getArchBits());
                case WINDOWS:
                    return getNatives().get("windows") == null ? null : getNatives().get("windows").replace("${arch}", env.getArchBits());
                case MAC_OS_X:
                    return getNatives().get("osx") == null ? null : getNatives().get("osx").replace("${arch}", env.getArchBits());
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    public String getFilename(Environment environment) {
        String nativeString = getNativeString(environment);
        if (nativeString != null) {
            return String.format("%s-%s-%s.jar",
                    getArtifact(), getVersion(), nativeString);
        }

        return String.format("%s-%s.jar", getArtifact(), getVersion());
    }

    public String getPath(Environment environment) {
        if(path!=null)
            return path;
        if(downloads!=null) {
            String nativeString = getNativeString(environment);
            if (nativeString != null) {
                path = downloads.getClassifiers().get(nativeString).getPath();
                return path;
            } else {
                path = downloads.getArtifact().getPath();
                return path;
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append(getGroup().replace('.', '/'));
        builder.append("/");
        builder.append(getArtifact());
        builder.append("/");
        builder.append(getVersion());
        builder.append("/");
        builder.append(getFilename(environment));
        String path = builder.toString();
        path = path.replace("${arch}", environment.getArchBits());
        return path;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Library library = (Library) o;

        if (name != null ? !name.equals(library.name) : library.name != null)
            return false;
        Environment env = Environment.getInstance();
        if(getNativeString(env)==library.getNativeString(env))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        String nativeString = getNativeString(Environment.getInstance());
        return name != null ? (name+(nativeString != null ? nativeString : "")).hashCode() : 0;
    }

    @Data
    public static class Rule {
        private Action action;
        private OS os;

        public boolean matches(Environment environment) {
            if (getOs() == null) {
                return true;
            } else {
                return getOs().matches(environment);
            }
        }
    }

    @Data
    public static class OS {
        private Platform platform;
        private Pattern version;

        @JsonProperty("name")
        @JsonDeserialize(using = PlatformDeserializer.class)
        @JsonSerialize(using = PlatformSerializer.class)
        public Platform getPlatform() {
            return platform;
        }

        public boolean matches(Environment environment) {
            return (getPlatform() == null || getPlatform().equals(environment.getPlatform())) &&
                    (getVersion() == null || getVersion().matcher(environment.getPlatformVersion()).matches());
        }
    }

    @Data
    public static class Extract {
        private List<String> exclude;
    }

    private enum Action {
        ALLOW,
        DISALLOW;

        @JsonCreator
        public static Action fromJson(String text) {
            return valueOf(text.toUpperCase());
        }

        @JsonValue
        public String toJson() {
            return name().toLowerCase();
        }
    }

}
