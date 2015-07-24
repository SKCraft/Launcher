/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import com.google.common.io.Closer;
import lombok.extern.java.Log;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

@Log
public final class LauncherUtils {

    private static final Pattern absoluteUrlPattern = Pattern.compile("^[A-Za-z0-9\\-]+://.*$");

    private LauncherUtils() {
    }

    public static String getStackTrace(Throwable t) {
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        t.printStackTrace(printWriter);
        return result.toString();
    }

    public static void checkInterrupted() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    public static Properties loadProperties(Class<?> clazz, String name, String extraProperty) throws IOException {
        Closer closer = Closer.create();
        Properties prop = new Properties();
        try {
            InputStream in = closer.register(clazz.getResourceAsStream(name));
            if (in != null) {
                prop.load(in);
                String extraPath = System.getProperty(extraProperty);
                if (extraPath != null) {
                    log.info("Loading extra properties for " +
                            clazz.getCanonicalName() + ":" + name + " from " + extraPath + "...");
                    in = closer.register(new BufferedInputStream(closer.register(new FileInputStream(extraPath))));
                    prop.load(in);
                }
            } else {
                throw new FileNotFoundException();
            }
        } finally {
            closer.close();
        }

        return prop;
    }

    public static URL concat(URL baseUrl, String url) throws MalformedURLException {
        if (absoluteUrlPattern.matcher(url).matches()) {
            return new URL(url);
        }

        int lastSlash = baseUrl.toExternalForm().lastIndexOf("/");
        if (lastSlash == -1) {
            return new URL(url);
        }

        int firstSlash = url.indexOf("/");
        if (firstSlash == 0) {
            boolean portSet = (baseUrl.getDefaultPort() == baseUrl.getPort() ||
                    baseUrl.getPort() == -1);
            String port = portSet ? "" : ":" + baseUrl.getPort();
            return new URL(baseUrl.getProtocol() + "://" + baseUrl.getHost()
                    + port + url);
        } else {
            return new URL(baseUrl.toExternalForm().substring(0, lastSlash + 1) + url);
        }
    }



    public static void interruptibleDelete(File file, List<File> failures) throws IOException, InterruptedException {
        checkInterrupted();

        if (file.isDirectory()) {
            File[] files = file.listFiles();

            if (files == null) {
                throw new IOException("Failed to list contents of " + file.getAbsolutePath());
            }

            for (File f : files) {
                interruptibleDelete(f, failures);
            }

            if (!file.delete()) {
                log.warning("Failed to delete " + file.getAbsolutePath());
                failures.add(file);
            }
        } else {
            if (!file.exists()) {
                throw new FileNotFoundException("Does not exist: " + file);
            }

            if (!file.delete()) {
                log.warning("Failed to delete " + file.getAbsolutePath());
                failures.add(file);
            }
        }
    }

}
