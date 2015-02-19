/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.bootstrap;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

public final class BootstrapUtils {

    private static final Pattern absoluteUrlPattern = Pattern.compile("^[A-Za-z0-9\\-]+://.*$");

    private BootstrapUtils() {
    }

    public static void checkInterrupted() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
        }
    }

    public static Properties loadProperties(Class<?> clazz, String name) throws IOException {
        Properties prop = new Properties();
        InputStream in = null;
        try {
            in = clazz.getResourceAsStream(name);
            prop.load(in);
        } finally {
            closeQuietly(in);
        }
        return prop;
    }

}
