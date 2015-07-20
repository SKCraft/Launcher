/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.staging;

import com.beust.jcommander.JCommander;
import com.skcraft.launcher.Launcher;
import org.eclipse.jetty.server.Server;

import java.io.File;

public class StagingServer {

    public static void main(String[] args) throws Exception {
        StagingArguments options = new StagingArguments();
        new JCommander(options, args);

        File wwwDir = options.getWwwDir();
        wwwDir.mkdirs();

        LocalHttpServerBuilder builder = new LocalHttpServerBuilder();
        builder.setBaseDir(wwwDir);
        builder.setPort(options.getPort());

        Server server = builder.build();
        server.start();

        if (options.isLaunch()) {
            Launcher.main(new String[0]);
        }
    }

}
