/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.server;

import lombok.Getter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

public class TestServer {

    @Getter private final Server server;

    public TestServer(Server server) {
        this.server = server;
    }

    public void start() throws Exception {
        getServer().start();
    }

    public int getLocalPort() {
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    public void stop() throws Exception {
        getServer().stop();
    }
}
