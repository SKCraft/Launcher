/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import com.skcraft.launcher.bootstrap.BootstrapArgs;
import com.skcraft.launcher.bootstrap.BootstrapConfig;
import com.skcraft.launcher.bootstrap.BootstrapUtils;
import com.skcraft.launcher.bootstrap.Downloader;
import com.skcraft.launcher.bootstrap.LauncherBinary;
import com.skcraft.launcher.bootstrap.LegacyBootstrapConfig;
import com.skcraft.launcher.bootstrap.PlatformDirFinder;
import com.skcraft.launcher.bootstrap.SharedLocale;
import com.skcraft.launcher.bootstrap.SimpleLogFormatter;
import com.skcraft.launcher.bootstrap.SwingHelper;
import com.skcraft.launcher.dirs.LauncherDirs;
import com.skcraft.launcher.util.Platform;
import lombok.Getter;
import lombok.extern.java.Log;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;

import static com.skcraft.launcher.bootstrap.SharedLocale.tr;

@Log
public class Bootstrap {

    private static final int BOOTSTRAP_VERSION = 2;

    @Getter
    private final LauncherDirs launcherDirs;
    @Getter
    private final boolean portable;
    @Getter
    private final Path binariesDir;
    @Getter
    private final Properties properties;
    private final String[] originalArgs;

    public static void main(String[] args) throws Throwable {
        SimpleLogFormatter.configureGlobalLogger();
        SharedLocale.loadBundle("com.skcraft.launcher.lang.Bootstrap", Locale.getDefault());

        boolean portable = isPortableMode();

        Bootstrap bootstrap = new Bootstrap(portable, args);
        try {
            bootstrap.cleanup();
            bootstrap.launch();
        } catch (Throwable t) {
            Bootstrap.log.log(Level.WARNING, "Error", t);
            Bootstrap.setSwingLookAndFeel();
            SwingHelper.showErrorDialog(null, tr("errors.bootstrapError"), tr("errorTitle"), t);
        }
    }

    public Bootstrap(boolean portable, String[] args) throws IOException {
        this.properties = BootstrapUtils.loadProperties(Bootstrap.class, "bootstrap.properties");

        var platform = detectPlatform();

        if (portable) {
            this.launcherDirs = LauncherDirs.singleFolder(Path.of("."));
        } else if (properties.getProperty("legacyMode", "true").equals("true")) {
            var config = new LegacyBootstrapConfig(properties.getProperty("homeFolderWindows"), properties.getProperty("homeFolderLinux"), properties.getProperty("homeFolder"));
            this.launcherDirs = PlatformDirFinder.getLegacy(platform, config);
        } else {
            var config = new BootstrapConfig(properties.getProperty("appFolderWindows"), properties.getProperty("appFolderUnix"));
            this.launcherDirs = PlatformDirFinder.get(platform, config);
        }

        this.portable = portable;
        this.binariesDir = launcherDirs.getLauncherDir();
        this.originalArgs = args;

        Files.createDirectories(binariesDir);
    }

    public void cleanup() {
        var matcher = FileSystems.getDefault().getPathMatcher("glob:*.tmp");
        try (var files = Files.find(binariesDir, 5, (path, $) -> matcher.matches(path))) {
            for (var path : (Iterable<Path>) files::iterator) {
                Files.delete(path);
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed cleaning up .tmp files", e);
        }
    }

    public List<LauncherBinary> findBinaries() throws IOException {
        try (var files = Files.find(binariesDir, 1, new LauncherBinary.Filter())) {
            return files.map(LauncherBinary::new).toList();
        }
    }

    public void launch() throws Throwable {
        var binaries = findBinaries();

        if (!binaries.isEmpty()) {
            launchExisting(binaries, true);
        } else {
            launchInitial();
        }
    }

    public void launchInitial() throws Exception {
        Bootstrap.log.info("Downloading the launcher...");
        Thread thread = new Thread(new Downloader(this));
        thread.start();
    }

    public void launchExisting(List<LauncherBinary> binaries, boolean redownload) throws Exception {
        Collections.sort(binaries);
        LauncherBinary working = null;
        Class<?> clazz = null;

        for (LauncherBinary binary : binaries) {
            var testFile = binary.getPath();
            try {
                testFile = binary.getExecutableJar();
                Bootstrap.log.info("Trying " + testFile.toAbsolutePath() + "...");
                clazz = load(testFile);
                Bootstrap.log.info("Launcher loaded successfully.");
                working = binary;
                break;
            } catch (Throwable t) {
                Bootstrap.log.log(Level.WARNING, "Failed to load " + testFile.toAbsolutePath(), t);
            }
        }

        if (working != null) {
            for (LauncherBinary binary : binaries) {
                if (working != binary) {
                    log.info("Removing " + binary.getPath() + "...");
                    binary.remove();
                }
            }

            execute(clazz);
        } else {
            if (redownload) {
                launchInitial();
            } else {
                throw new IOException("Failed to find launchable .jar");
            }
        }
    }

    public void execute(Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        var method = clazz.getDeclaredMethod("bootstrapMain", BootstrapArgs.class);

        log.info("Launching via bootstrapMain with data dir '%s'".formatted(launcherDirs.dataDir()));
        method.invoke(null, new BootstrapArgs(launcherDirs, BOOTSTRAP_VERSION, originalArgs));
    }

    public Class<?> load(Path jarFile) throws MalformedURLException, ClassNotFoundException {
        URL[] urls = new URL[]{jarFile.toUri().toURL()};
        URLClassLoader child = new URLClassLoader(urls, this.getClass().getClassLoader());
        Class<?> clazz = Class.forName(getProperties().getProperty("launcherClass"), true, child);
        return clazz;
    }

    public static void setSwingLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
        }
    }

    private static boolean isPortableMode() {
        return new File("portable.txt").exists();
    }

    /**
     * Detect the current platform.
     *
     * @return the current platform
     */
    public static Platform detectPlatform() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win"))
            return Platform.WINDOWS;
        if (osName.contains("mac"))
            return Platform.MAC_OS_X;
        if (osName.contains("solaris") || osName.contains("sunos"))
            return Platform.SOLARIS;
        if (osName.contains("linux"))
            return Platform.LINUX;
        if (osName.contains("unix"))
            return Platform.LINUX;
        if (osName.contains("bsd"))
            return Platform.LINUX;

        return Platform.UNKNOWN;
    }
}
