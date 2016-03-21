/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */
package com.skcraft.launcher;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import com.skcraft.launcher.launch.JavaRuntimeFinder;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.selfupdate.ComparableVersion;
import java.io.File;
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

    private boolean offlineEnabled = false;
    private String jvmPath;
    private String jvmArgs;
    private int minMemory = 1024;
    private int maxMemory = 2048;
    private int permGen = 256;
    private int launcherWindowWidth = -1;
    private int launcherWindowHeight = -1;
    private int launcherExtendedState = 0;
    private int windowWidth = 854;
    private int widowHeight = 480;
    private String gameKey;

    public static void setImplicitExit() {
        javafx.application.Platform.setImplicitExit(false);
    }

    public void checkVaules() {
        if (windowWidth <= 100) {
            windowWidth = 854;
        }

        if (widowHeight <= 50) {
            windowWidth = 480;
        }
    }

    public void setupMemory() {
        boolean changed = false;
        if (minMemory <= 32) {
            minMemory = 256;
            changed = true;
        }

        if (maxMemory <= 0) {
            maxMemory = 1024;
            changed = true;
        }

        if (permGen <= 128) {
            permGen = 128;
            changed = true;
        }

        if (minMemory > maxMemory) {
            maxMemory = minMemory;
            changed = true;
        }

        long currnetAmmount = -1;
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            if (method.getName().startsWith("getFreePhysicalMemorySize")
                    && Modifier.isPublic(method.getModifiers())) {

                try {
                    currnetAmmount = (Long) method.invoke(operatingSystemMXBean);
                } catch (Exception e) {
                }
            } // if
        } // for

        if (currnetAmmount > 0) {
            int memoryMB = (int) (currnetAmmount / (1024 * 1024));

            if (memoryMB <= minMemory) {
                memoryMB = ((memoryMB / 2) / 256) * 256;
                minMemory = memoryMB;
                if (memoryMB <= 512) {
                    minMemory = 128;
                }
                changed = true;

            }
        }

        if (changed) {
            Persistence.commitAndForget(this);
        }

        long maxAmmount = -1;
        operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            if (method.getName().startsWith("getTotalPhysicalMemorySize")
                    && Modifier.isPublic(method.getModifiers())) {

                try {
                    maxAmmount = (Long) method.invoke(operatingSystemMXBean);
                } catch (Exception e) {
                }
            } // if
        } // for

        if (maxAmmount > 0) {
            int memoryMB = (int) (maxAmmount / (1024 * 1024));
            if (memoryMB <= maxMemory) {
                maxMemory = memoryMB;
            }
        }
    }

    public void setupJVMPath() {

        String oldJvmPath = jvmPath;
        File file = JavaRuntimeFinder.findBestJavaPath();
        if (file != null) {
            jvmPath = file.getAbsolutePath();
        } else if (!Strings.isNullOrEmpty(oldJvmPath) && new File(oldJvmPath).exists()) {
            return;
        }
        Persistence.commitAndForget(this);

    }

    public void setupJVMargs() {
        if (Strings.isNullOrEmpty(jvmArgs)) {
            jvmArgs = "-XX:+UseParNewGC -XX:+UseConcMarkSweepGC";
        }
        if (!jvmArgs.contains("-XX:+UseParNewGC")) {
            jvmArgs += " -XX:+UseParNewGC";
            return;
        }
        if (!jvmArgs.contains("-XX:+UseConcMarkSweepGC")) {
            jvmArgs += " -XX:+UseConcMarkSweepGC";
            return;
        }

    }

    public static boolean checkJvmPath(File path) {
        return path.exists();
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
