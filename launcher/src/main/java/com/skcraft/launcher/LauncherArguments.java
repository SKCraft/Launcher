/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import com.beust.jcommander.Parameter;
import lombok.Data;

import java.io.File;

/**
 * The command line arguments that the launcher accepts.
 */
@Data
public class LauncherArguments {

    @Parameter(names = "--dir")
    private File dir;

    @Parameter(names = "--bootstrap-version")
    private Integer bootstrapVersion;

    @Parameter(names = "--portable")
    private boolean portable;

}
