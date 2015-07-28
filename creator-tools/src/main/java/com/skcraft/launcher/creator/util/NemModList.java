/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.skcraft.launcher.util.HttpRequest;
import lombok.Data;
import lombok.Getter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class NemModList {

    @Getter
    private Map<String, ModEntry> mods = ImmutableMap.of();

    public void load(String version) throws IOException, InterruptedException {
        checkNotNull(version, "version");

        List<ModEntry> mods = HttpRequest.get(HttpRequest.url("https://bot.notenoughmods.com/" + version + ".json"))
                .execute()
                .expectResponseCode(200)
                .returnContent()
                .asJson(new TypeReference<List<ModEntry>>() {});

        Map<String, ModEntry> index = Maps.newHashMap();

        for (ModEntry entry : mods) {
            index.put(entry.getModId(), entry);
        }

        this.mods = Collections.unmodifiableMap(index);
    }

    @Nullable
    public ModEntry get(String modId) {
        checkNotNull(modId, "modId");
        return mods.get(modId);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModEntry {

        @JsonProperty("modid")
        private String modId;
        private String name;
        private List<String> aliases;

        @JsonProperty("version")
        private String latestVersion;
        @JsonProperty("dev")
        private String latestDevVersion;
        @JsonProperty("prevversion")
        private String previousVersion;

        private List<String> dependencies;

        @JsonProperty("longurl")
        private URL url;
        private List<String> tags;
        private String comment;
        private String author;

        private String license;
        private URL repo;

        @JsonProperty("added_at")
        private Date addedAt;
        @JsonProperty("lastupdated")
        private Date lastUpdated;

    }

}
