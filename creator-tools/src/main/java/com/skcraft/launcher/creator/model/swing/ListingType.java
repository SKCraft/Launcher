/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.model.swing;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.skcraft.launcher.creator.model.creator.ManifestEntry;
import com.skcraft.launcher.model.modpack.ManifestInfo;
import com.skcraft.launcher.model.modpack.PackageList;
import com.skcraft.launcher.persistence.Persistence;
import lombok.Getter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public enum ListingType {

    STATIC("packages.json (static)", false, "packages.json") {
        @Override
        public String generate(List<ManifestEntry> entries) throws IOException {
            PackageList list = new PackageList();
            list.setPackages(Lists.newArrayList());
            list.setMinimumVersion(PackageList.MIN_VERSION);
            for (ManifestEntry entry : entries) {
                if (entry.getGameKeys().isEmpty()) {
                    list.getPackages().add(entry.getManifestInfo());
                }
            }
            return Persistence.writeValueAsString(list, Persistence.L2F_LIST_PRETTY_PRINTER);
        }
    },
    PHP("packages.php (requires PHP on web server) ", true, "packages.php") {
        @Override
        public String generate(List<ManifestEntry> entries) throws IOException {
            StringBuilder builder = new StringBuilder();
            builder.append("<?php\r\n");
            builder.append("$keys = isset($_GET['key']) ? array_map('trim', explode(',', strtolower($_GET['key']))) : array();\r\n");
            builder.append("$packages = array();\r\n\r\n");

            for (ManifestEntry entry : entries) {
                ManifestInfo info = entry.getManifestInfo();
                List<String> keys = entry.getGameKeys();

                if (!keys.isEmpty()) {
                    builder.append("if (count(array_intersect(array(").append(escapeKeys(keys)).append("), $keys)) > 0)\r\n");
                }

                builder.append("$packages[] = array(\r\n");
                builder.append("    'name' => '").append(escape(info.getName())).append("',\r\n");
                if (info.getTitle() != null) {
                    builder.append("    'title' => '").append(escape(info.getTitle())).append("',\r\n");
                }
                builder.append("    'version' => '").append(escape(info.getVersion())).append("',\r\n");
                builder.append("    'priority' => ").append(info.getPriority()).append(",\r\n");
                builder.append("    'location' => '").append(escape(info.getLocation())).append("',\r\n");
                builder.append(");\r\n\r\n");
            }

            builder.append("$out = array('minimumVersion' => ").append(PackageList.MIN_VERSION).append(", 'packages' => $packages);\r\n");
            builder.append("header('Content-Type: text/plain; charset=utf-8');\r\n");
            builder.append("echo json_encode($out);\r\n");
            return builder.toString();
        }

        private String escape(String t) {
            return t.replaceAll("[\\\\\"']", "\\$0").replace("\r", "\\r").replace("\n", "\\n");
        }

        private String escapeKeys(List<String> list) {
            return Joiner.on(", ").join(list.stream().map(s -> "'" + escape(s.toLowerCase()) + "'").collect(Collectors.toList()));
        }
    };

    @Getter private final String name;
    @Getter private final boolean gameKeyCompatible;
    @Getter private final String filename;

    ListingType(String name, boolean gameKeyCompatible, String filename) {
        this.name = name;
        this.gameKeyCompatible = gameKeyCompatible;
        this.filename = filename;
    }

    @Override
    public String toString() {
        return name;
    }

    public abstract String generate(List<ManifestEntry> entries) throws IOException;

}
