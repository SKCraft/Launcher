/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */
package com.skcraft.launcher;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import com.skcraft.launcher.launch.JavaRuntimeFinder;
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
    
    public void checkVaules()
    {
        if (windowWidth <= 100)
        {
            windowWidth = 854;
        }
        
        if (widowHeight <= 50)
        {
            windowWidth = 480;
        }
    }
    public void setupMemory() {
        if (minMemory <= 32) {
            minMemory = 256;
        }

        if (maxMemory <= 0) {
            maxMemory = 1024;
        }

        if (permGen <= 128) {
            permGen = 128;
        }

        if (minMemory > maxMemory) {
            maxMemory = minMemory;
        }

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
            if (memoryMB <= maxMemory) {
                memoryMB = (memoryMB / 256) * 256;
                maxMemory = memoryMB;
                if (memoryMB <= 512) {
                    minMemory = 128;
                }

            }
        }

        

    }

    public void setupJVMPath() {

        if (!Strings.isNullOrEmpty(jvmPath) && new File(jvmPath).exists()) {
            return;
        }

        String OS = System.getProperty("os.name").toUpperCase();
        if (OS.contains("WIN")) {
            jvmPath = JavaRuntimeFinder.findBestJavaPath().getAbsolutePath();
            File file = new File(System.getenv("ProgramFiles").charAt(0) + ":/Program Files/Java/");
            if (!file.exists()) {
                return;
            }
            File[] listFiles = file.listFiles();

            for (File file1 : listFiles) {
                if (file1.getName().toLowerCase().contains("jre7")) {
                    jvmPath = file1.getAbsolutePath();
                    return;
                }
            }

            String best = null;
            for (File file1 : listFiles) {

                if (file1.getName().toLowerCase().contains("jre1.8")) {
                    if (best == null) {
                        best = file1.getName();
                    } else {
                        ComparableVersion version1 = new ComparableVersion(best);
                        ComparableVersion version2 = new ComparableVersion(file.getName());
                        if (version1.compareTo(version2) < 0) {
                            best = file1.getName();
                        }
                    }
                }

            }
            if (best != null) {
                jvmPath = new File(file, best).getAbsolutePath();
                return;
            }

            for (File file1 : listFiles) {
                if (file1.getName().toLowerCase().contains("jre6")) {
                    jvmPath = file1.getAbsolutePath();
                    return;
                }
            }

            //C:\Program Files\Java\jre7\bin\javaw.exe
            //return System.getenv("APPDATA");
        } else if (OS.contains("MAC")) {
            //return System.getProperty("user.home") + "/Library/Application " + "Support" + "/";
        } else if (OS.contains("NUX")) {
            //return System.getProperty("user.home");
        }
        // return System.getProperty("user.dir");

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
