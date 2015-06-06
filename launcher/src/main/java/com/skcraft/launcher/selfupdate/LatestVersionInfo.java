/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.selfupdate;

import lombok.Data;

import java.net.URL;

@Data
public class LatestVersionInfo {

    private String version;
    private URL url;

}
