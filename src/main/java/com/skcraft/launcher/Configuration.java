/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */
package com.skcraft.launcher;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import lombok.Data;

/**
 * The configuration for the launcher.
 * </p>
 * Default values are stored as field values. Note that if a default value is
 * changed after the launcher has been deployed, it may not take effect for
 * users who have already used the launcher because the old default values would
 * have been written to disk.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Configuration {

    private boolean offlineEnabled = true;
    private String jvmPath;
    private String jvmArgs;
    private int minMemory = 1024;
    private int maxMemory = 2048;
    private int permGen = 256;
    private int windowWidth = 854;
    private int widowHeight = 480;
    private boolean proxyEnabled = false;
    private String proxyHost = "localhost";
    private int proxyPort = 8080;
    private String proxyUsername;
    private String proxyPassword;
    private String gameKey;

    public void setupMemory() {
        long value = -1;
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            if (method.getName().startsWith("getFreePhysicalMemorySize")
                    && Modifier.isPublic(method.getModifiers())) {

                try {
                    value = (Long) method.invoke(operatingSystemMXBean);
                } catch (Exception e) {
                }
            } // if
        } // for

        if (value > 0) {
            int memoryMB = (int) (value / (1024 * 1024));
            if (memoryMB <= 2500) {
                memoryMB = (memoryMB / 256) * 256;
                maxMemory = memoryMB;
                if (memoryMB >= 512) {
                    minMemory = 256;
                } else {
                    minMemory = 128;
                }

            }
        }
        if (System.getProperty("sun.arch.data.model").equalsIgnoreCase("32") && maxMemory > 1244)
        {
            maxMemory = 1244;
        }

    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
