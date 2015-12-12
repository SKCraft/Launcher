/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.model.creator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.File;
import java.net.URL;
import java.util.regex.Pattern;

@Data
public class ModFile {

    private File file;
    private String modId;
    private String name;
    private String gameVersion;
    private String version;
    private String latestVersion;
    private String latestDevVersion;
    private URL url;

    @JsonIgnore
    public String getCleanVersion() {
        String version = getVersion();
        return version != null ?
                version
                        .replaceAll("^" + Pattern.quote(gameVersion) + "\\-", "")
                        .replaceAll("\\-" + Pattern.quote(gameVersion) + "$", "")
                : null;
    }

}
