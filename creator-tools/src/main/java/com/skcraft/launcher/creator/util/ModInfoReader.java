/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.google.common.io.Closer;
import lombok.Data;
import lombok.extern.java.Log;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Reads the mod information file from a mod .jar, with support for both Forge
 * and LiteLoader.
 */
@Log
public class ModInfoReader {

    private static final String FORGE_INFO_FILENAME = "mcmod.info";
    private static final String LITELOADER_INFO_FILENAME = "litemod.json";
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Detect the mods listed in the given .jar
     *
     * @param file The file
     * @return A list of detected mods
     */
    public List<? extends ModInfo> detectMods(File file) {
        Closer closer = Closer.create();

        try {
            FileInputStream fis = closer.register(new FileInputStream(file));
            BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
            ZipInputStream zis = closer.register(new ZipInputStream(bis));

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equalsIgnoreCase(FORGE_INFO_FILENAME)) {
                    List<ForgeModInfo> mods;
                    String content = CharStreams.toString(new InputStreamReader(zis, Charsets.UTF_8));

                    try {
                        mods = mapper.readValue(content, ForgeModManifest.class).getMods();
                    } catch (JsonMappingException | JsonParseException e) {
                        mods = mapper.readValue(content, new TypeReference<List<ForgeModInfo>>() {});
                    }

                    if (mods != null) {
                        // Ignore "examplemod"
                        return Collections.unmodifiableList(
                                mods.stream()
                                        .filter(info -> !info.getModId().equals("examplemod"))
                                        .collect(Collectors.toList()));
                    } else {
                        return Collections.emptyList();
                    }

                } else if (entry.getName().equalsIgnoreCase(LITELOADER_INFO_FILENAME)) {
                    String content = CharStreams.toString(new InputStreamReader(zis, Charsets.UTF_8));
                    return new ImmutableList.Builder<ModInfo>().add(mapper.readValue(content, LiteLoaderModInfo.class)).build();
                }
            }

            return Collections.emptyList();
        } catch (JsonMappingException e) {
            log.log(Level.WARNING, "Unknown format " + FORGE_INFO_FILENAME + " file in " + file.getAbsolutePath(), e);
            return Collections.emptyList();
        } catch (JsonParseException e) {
            log.log(Level.WARNING, "Corrupt " + FORGE_INFO_FILENAME + " file in " + file.getAbsolutePath(), e);
            return Collections.emptyList();
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to read " + file.getAbsolutePath(), e);
            return Collections.emptyList();
        } finally {
            try {
                closer.close();
            } catch (IOException ignored) {
            }
        }
    }

    public interface ModInfo {

        String getModId();
        String getName();
        String getDescription();
        String getVersion();
        String getGameVersion();
        String getUrl();

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ForgeModInfo implements ModInfo {

        @JsonProperty("modid")
        private String modId;
        private String name;
        private String description;
        private String version;
        @JsonProperty("mcversion")
        private String gameVersion;
        private String url;
        private String updateUrl;
        private List<String> authorList;
        private String credits;
        private List<String> dependencies;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ForgeModManifest {

        @JsonProperty("modListVersion")
        private int version;
        @JsonProperty("modList")
        private List<ForgeModInfo> mods;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class LiteLoaderModInfo implements ModInfo {

        private String name;
        private String version;
        @JsonProperty("mcversion")
        private String gameVersion;
        private String revision;
        private String author;
        private String description;

        @JsonIgnore
        @Override
        public String getModId() {
            return null;
        }

        @Override
        public String getUrl() {
            return null;
        }

    }


}
