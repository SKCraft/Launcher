/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.launch;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
import com.skcraft.launcher.model.minecraft.JavaVersion;
import com.skcraft.launcher.util.Environment;
import com.skcraft.launcher.util.EnvironmentParser;
import com.skcraft.launcher.util.Platform;
import com.skcraft.launcher.util.WinRegistry;
import com.sun.jna.platform.win32.WinReg;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Finds the best Java runtime to use.
 */
@Log
public final class JavaRuntimeFinder {

    private JavaRuntimeFinder() {
    }

    /**
     * Get all available Java runtimes on the system
     * @return List of available Java runtimes sorted by newest first
     */
    public static List<JavaRuntime> getAvailableRuntimes() {
        Environment env = Environment.getInstance();
        Set<JavaRuntime> entries = new HashSet<>();
        Set<File> launcherDirs = new HashSet<>();

        if (env.getPlatform() == Platform.WINDOWS) {
            try {
                String launcherPath = WinRegistry.readString(WinReg.HKEY_CURRENT_USER,
                        "SOFTWARE\\Mojang\\InstalledProducts\\Minecraft Launcher", "InstallLocation");

                launcherDirs.add(new File(launcherPath));
            } catch (Throwable err) {
                log.log(Level.WARNING, "Failed to read launcher location from registry", err);
            }

            String programFiles = Objects.equals(env.getArchBits(), "64")
                ? System.getenv("ProgramFiles(x86)")
                : System.getenv("ProgramFiles");

            // Mojang likes to move the java runtime directory
            launcherDirs.add(new File(programFiles, "Minecraft"));
            launcherDirs.add(new File(programFiles, "Minecraft Launcher"));
            launcherDirs.add(new File(System.getenv("LOCALAPPDATA"), "Packages\\Microsoft.4297127D64EC6_8wekyb3d8bbwe\\LocalCache\\Local"));

            getEntriesFromRegistry(entries, "SOFTWARE\\JavaSoft\\Java Runtime Environment");
            getEntriesFromRegistry(entries, "SOFTWARE\\JavaSoft\\Java Development Kit");
            getEntriesFromRegistry(entries, "SOFTWARE\\JavaSoft\\JDK");
        } else if (env.getPlatform() == Platform.LINUX) {
            launcherDirs.add(new File(System.getenv("HOME"), ".minecraft"));

            String javaHome = System.getenv("JAVA_HOME");
            if (javaHome != null) {
                entries.add(getRuntimeFromPath(javaHome));
            }

            File[] runtimesList = new File("/usr/lib/jvm").listFiles();
            if (runtimesList != null) {
                Arrays.stream(runtimesList).map(file -> {
                    try {
                        return file.getCanonicalFile();
                    } catch (IOException exception) {
                        return file;
                    }
                }).distinct().forEach(file -> entries.add(getRuntimeFromPath(file.getAbsolutePath())));
            }
        } else if (env.getPlatform() == Platform.MAC_OS_X) {
            launcherDirs.add(new File(System.getenv("HOME"), "Library/Application Support/minecraft"));

            try {
                Process p = Runtime.getRuntime().exec("/usr/libexec/java_home -X");
                NSArray root = (NSArray) PropertyListParser.parse(p.getInputStream());
                NSObject[] arr = root.getArray();
                for (NSObject obj : arr) {
                    NSDictionary dict = (NSDictionary) obj;
                    entries.add(new JavaRuntime(
                        new File(dict.objectForKey("JVMHomePath").toString()).getAbsoluteFile(),
                        dict.objectForKey("JVMVersion").toString(),
                        isArch64Bit(dict.objectForKey("JVMArch").toString())
                    ));
                }
            } catch (Throwable err) {
                log.log(Level.WARNING, "Failed to parse java_home command", err);
            }
        } else {
            return Collections.emptyList();
        }

        for (File install : launcherDirs) {
            File runtimes = new File(install, "runtime");
            File[] runtimeList = runtimes.listFiles();
            if (runtimeList != null) {
                for (File potential : runtimeList) {
                    String runtimeName = potential.getName();
                    if (runtimeName.startsWith("jre-x")) {
                        boolean is64Bit = runtimeName.equals("jre-x64");

                        JavaRuntime runtime = new JavaRuntime(potential.getAbsoluteFile(), readVersionFromRelease(potential), is64Bit);
                        runtime.setMinecraftBundled(true);
                        entries.add(runtime);
                    } else {
                        String[] children = potential.list((dir, name) -> new File(dir, name).isDirectory());
                        if (children == null || children.length != 1) continue;
                        String platformName = children[0];

                        File javaDir = new File(potential, String.format("%s/%s", platformName, runtimeName));
                        if (env.getPlatform() == Platform.MAC_OS_X) {
                            javaDir = new File(javaDir, "jre.bundle/Contents/Home");
                        }

                        String arch = readArchFromRelease(javaDir);
                        boolean is64Bit = isArch64Bit(arch);

                        JavaRuntime runtime = new JavaRuntime(javaDir.getAbsoluteFile(), readVersionFromRelease(javaDir), is64Bit);
                        runtime.setMinecraftBundled(true);
                        entries.add(runtime);
                    }
                }
            }
        }

        return entries.stream().sorted().collect(Collectors.toList());
    }

    /**
     * Return the path to the best found JVM location.
     *
     * @return the JVM location, or null
     */
    public static File findBestJavaPath() {
        List<JavaRuntime> entries = getAvailableRuntimes();
        if (entries.size() > 0) {
            return new File(entries.get(0).getDir(), "bin");
        }
        
        return null;
    }

    public static Optional<JavaRuntime> findBestJavaRuntime(JavaVersion targetVersion) {
        List<JavaRuntime> entries = getAvailableRuntimes();

        return entries.stream().sorted()
                .filter(runtime -> runtime.getMajorVersion() == targetVersion.getMajorVersion())
                .findFirst();
    }

    public static Optional<JavaRuntime> findAnyJavaRuntime() {
        return getAvailableRuntimes().stream().sorted().findFirst();
    }

    public static JavaRuntime getRuntimeFromPath(String path) {
        File target = new File(path);

        if (target.isFile()) {
            // Probably referring directly to bin/java, back up two levels
            target = target.getParentFile().getParentFile();
        } else if (target.getName().equals("bin")) {
            // Probably copied the bin directory that java.exe is in
            target = target.getParentFile();
        }

        {
            File jre = new File(target, "jre/release");
            if (jre.isFile()) {
                target = jre.getParentFile();
            }
        }

        return new JavaRuntime(target, readVersionFromRelease(target), guessIf64BitWindows(target));
    }
    
    private static void getEntriesFromRegistry(Collection<JavaRuntime> entries, String basePath)
            throws IllegalArgumentException {
        try {
            List<String> subKeys = WinRegistry.readStringSubKeys(WinReg.HKEY_LOCAL_MACHINE, basePath);
            for (String subKey : subKeys) {
                JavaRuntime entry = getEntryFromRegistry(basePath, subKey);
                if (entry != null) {
                    entries.add(entry);
                }
            }
        } catch (Throwable err) {
            log.log(Level.INFO, "Failed to read Java locations from registry in " + basePath);
        }
    }
    
    private static JavaRuntime getEntryFromRegistry(String basePath, String version) {
        String regPath = basePath + "\\" + version;
        String path = WinRegistry.readString(WinReg.HKEY_LOCAL_MACHINE, regPath, "JavaHome");
        File dir = new File(path);
        if (dir.exists() && new File(dir, "bin/java.exe").exists()) {
            return new JavaRuntime(dir, version, guessIf64BitWindows(dir));
        } else {
            return null;
        }
    }
    
    private static boolean guessIf64BitWindows(File path) {
        try {
            String programFilesX86 = System.getenv("ProgramFiles(x86)");
            return programFilesX86 == null || !path.getCanonicalPath().startsWith(new File(programFilesX86).getCanonicalPath());
        } catch (IOException ignored) {
            return false;
        }
    }

    private static boolean isArch64Bit(String string) {
        return string == null || string.matches("x64|x86_64|amd64|aarch64");
    }

    private static String readVersionFromRelease(File javaPath) {
        File releaseFile = new File(javaPath, "release");
        if (releaseFile.exists()) {
            try {
                Map<String, String> releaseDetails = EnvironmentParser.parse(releaseFile);

                return releaseDetails.get("JAVA_VERSION");
            } catch (IOException e) {
                log.log(Level.WARNING, "Failed to read release file " + releaseFile.getAbsolutePath(), e);
                return null;
            }
        }

        return null;
    }

    private static String readArchFromRelease(File javaPath) {
        File releaseFile = new File(javaPath, "release");
        if (releaseFile.exists()) {
            try {
                Map<String, String> releaseDetails = EnvironmentParser.parse(releaseFile);

                return releaseDetails.get("OS_ARCH");
            } catch (IOException e) {
                log.log(Level.WARNING, "Failed to read release file " + releaseFile.getAbsolutePath(), e);
                return null;
            }
        }

        return null;
    }
}
