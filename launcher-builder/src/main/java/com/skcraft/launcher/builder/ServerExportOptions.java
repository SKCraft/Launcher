/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.builder;

import com.beust.jcommander.Parameter;
import lombok.Data;

import java.io.File;

@Data
public class ServerExportOptions {

    @Parameter(names = "--source", required = true)
    private File sourceDir;
    @Parameter(names = "--dest", required = true)
    private File destDir;

}
