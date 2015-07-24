/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skcraft.launcher.selfupdate.LatestVersionInfo;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

class LatestHandler extends AbstractHandler {

    private final ObjectMapper mapper;

    public LatestHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/plain; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        LatestVersionInfo info = new LatestVersionInfo();
        info.setVersion("0.0.0");
        info.setUrl(new URL("http://localhost"));
        mapper.writeValue(response.getWriter(), info);

        baseRequest.setHandled(true);
    }

}
