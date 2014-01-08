/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Configuration {

    private boolean offlineEnabled = false;
    private String jvmPath;
    private String jvmArgs;
    private int minMemory = 1024;
    private int maxMemory = 1024;
    private int permGen = 128;
    private int windowWidth = 854;
    private int widowHeight = 480;
    private boolean proxyEnabled = false;
    private String proxyHost = "localhost";
    private int proxyPort = 8080;
    private String proxyUsername;
    private String proxyPassword;
    private String gameKey;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
