/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.buildtools;

import com.beust.jcommander.Parameter;
import lombok.Data;

import java.io.File;

@Data
public class ToolArguments {

    @Parameter(names = "--dir")
    private File dir = new File(".");

    @Parameter(names = "--port")
    private int port = 0;

}
