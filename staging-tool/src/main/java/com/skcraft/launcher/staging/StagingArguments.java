/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.staging;

import com.beust.jcommander.Parameter;
import lombok.Data;

import java.io.File;

/**
 * The command line arguments that the staging tool accepts.
 */
@Data
public class StagingArguments {

    @Parameter(names = "--www-dir")
    private File wwwDir = new File(".");

    @Parameter(names = "--port")
    private Integer port = 28888;

    @Parameter(names = "--launch")
    private boolean launch = false;

}
