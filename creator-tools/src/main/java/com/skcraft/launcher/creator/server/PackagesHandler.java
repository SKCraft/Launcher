/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.server;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skcraft.launcher.model.modpack.Manifest;
import com.skcraft.launcher.model.modpack.ManifestInfo;
import com.skcraft.launcher.model.modpack.PackageList;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

class PackagesHandler extends AbstractHandler {

    private final ObjectMapper mapper;
    private final File baseDir;

    public PackagesHandler(ObjectMapper mapper, File baseDir) {
        this.mapper = mapper;
        this.baseDir = baseDir;
    }

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/plain; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        List<ManifestInfo> packages = Lists.newArrayList();
        PackageList packageList = new PackageList();
        packageList.setPackages(packages);

        File[] files = baseDir.listFiles(new PackageFileFilter());
        if (files != null) {
            for (File file : files) {
                Manifest manifest = mapper.readValue(file, Manifest.class);
                ManifestInfo info = new ManifestInfo();
                info.setName(manifest.getName());
                info.setTitle(manifest.getTitle());
                info.setVersion(manifest.getVersion());
                info.setLocation(file.getName());
                packages.add(info);
            }
        }

        mapper.writeValue(response.getWriter(), packageList);
        baseRequest.setHandled(true);
    }

    private static class PackageFileFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            return pathname.getName().toLowerCase().endsWith(".json");
        }
    }
}
