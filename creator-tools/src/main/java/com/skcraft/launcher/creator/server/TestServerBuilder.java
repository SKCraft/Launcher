/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

import java.io.File;

public class TestServerBuilder {

    private File baseDir = new File(".");
    private int port = 28888;

    public File getBaseDir() {
        return baseDir;
    }

    public TestServerBuilder setBaseDir(File baseDir) {
        this.baseDir = baseDir;
        return this;
    }

    public int getPort() {
        return port;
    }

    public TestServerBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public TestServer build() {
        Server server = new Server(port);

        ObjectMapper mapper = new ObjectMapper();

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setResourceBase(baseDir.getAbsolutePath());
        resourceHandler.setMinMemoryMappedContentLength(-1); // Causes file locking on Windows

        ContextHandler rootContext = new ContextHandler();
        rootContext.setContextPath("/");
        rootContext.setHandler(resourceHandler);

        ContextHandler packagesContext = new ContextHandler("/packages.json");
        packagesContext.setAllowNullPathInfo(true);
        packagesContext.setHandler(new PackagesHandler(mapper, baseDir));

        ContextHandler latestContext = new ContextHandler("/latest.json");
        latestContext.setAllowNullPathInfo(true);
        latestContext.setHandler(new LatestHandler(mapper));

        ContextHandler newsContext = new ContextHandler("/news.html");
        newsContext.setAllowNullPathInfo(true);
        newsContext.setHandler(new NewsHandler());

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[]{packagesContext, latestContext, newsContext, rootContext});

        GzipHandler gzip = new GzipHandler();
        server.setHandler(gzip);
        gzip.setHandler(contexts);

        return new TestServer(server);
    }

}
