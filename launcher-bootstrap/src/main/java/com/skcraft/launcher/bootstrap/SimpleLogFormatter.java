/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.bootstrap;

import lombok.extern.java.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.*;

@Log
public final class SimpleLogFormatter extends Formatter {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        sb.append("[")
            .append(record.getLevel().getLocalizedName().toLowerCase())
            .append("] ")
            .append(formatMessage(record))
            .append(LINE_SEPARATOR);

        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception e) {
            }
        }

        return sb.toString();
    }
    
    public static void configureGlobalLogger() {
        Logger globalLogger = Logger.getLogger("");

        // Set formatter
        for (Handler handler : globalLogger.getHandlers()) {
            handler.setFormatter(new SimpleLogFormatter());
        }

        // Set level
        String logLevel = System.getProperty(
                SimpleLogFormatter.class.getCanonicalName() + ".logLevel", "INFO");
        try {
            Level level = Level.parse(logLevel);
            globalLogger.setLevel(level);
        } catch (IllegalArgumentException e) {
            log.log(Level.WARNING, "Invalid log level of " + logLevel, e);
        }
    }
    
}