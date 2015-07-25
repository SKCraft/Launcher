/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import com.skcraft.launcher.bootstrap.*;
import lombok.Getter;
import lombok.extern.java.Log;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;

import static com.skcraft.launcher.bootstrap.SharedLocale.tr;

@Log
public class Bootstrap {

    private static final int BOOTSTRAP_VERSION = 1;

    @Getter private final File baseDir;
    @Getter private final boolean portable;
    @Getter private final File binariesDir;
    @Getter private final Properties properties;
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

        File baseDir = portable ? new File(".") : getUserLauncherDir();

        this.baseDir = baseDir;
        this.portable = portable;
        this.binariesDir = new File(baseDir, "launcher");
        this.originalArgs = args;

        binariesDir.mkdirs();
    }

    public void cleanup() {
        File[] files = binariesDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".tmp");
            }
        });

        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public void launch() throws Throwable {
        File[] files = binariesDir.listFiles(new LauncherBinary.Filter());
        List<LauncherBinary> binaries = new ArrayList<LauncherBinary>();

        if (files != null) {
            for (File file : files) {
                Bootstrap.log.info("Found " + file.getAbsolutePath() + "...");
                binaries.add(new LauncherBinary(file));
            }
        }

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
            File testFile = binary.getPath();
            try {
                testFile = binary.getExecutableJar();
                Bootstrap.log.info("Trying " + testFile.getAbsolutePath() + "...");
                clazz = load(testFile);
                Bootstrap.log.info("Launcher loaded successfully.");
                working = binary;
                break;
            } catch (Throwable t) {
                Bootstrap.log.log(Level.WARNING, "Failed to load " + testFile.getAbsoluteFile(), t);
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

    public void execute(Class<?> clazz) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = clazz.getDeclaredMethod("main", String[].class);
        String[] launcherArgs;

        if (portable) {
            launcherArgs = new String[] {
                    "--portable",
                    "--dir",
                    baseDir.getAbsolutePath(),
                    "--bootstrap-version",
                    String.valueOf(BOOTSTRAP_VERSION) };
        } else {
            launcherArgs = new String[] {
                    "--dir",
                    baseDir.getAbsolutePath(),
                    "--bootstrap-version",
                    String.valueOf(BOOTSTRAP_VERSION)  };
        }

        String[] args = new String[originalArgs.length + launcherArgs.length];
        System.arraycopy(launcherArgs, 0, args, 0, launcherArgs.length);
        System.arraycopy(originalArgs, 0, args, launcherArgs.length, originalArgs.length);

        log.info("Launching with arguments " + Arrays.toString(args));

        method.invoke(null, new Object[] { args });
    }

    public Class<?> load(File jarFile) throws MalformedURLException, ClassNotFoundException {
        URL[] urls = new URL[] { jarFile.toURI().toURL() };
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

    private static File getFileChooseDefaultDir() {
        JFileChooser chooser = new JFileChooser();
        FileSystemView fsv = chooser.getFileSystemView();
        return fsv.getDefaultDirectory();
    }

    private File getUserLauncherDir() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return new File(getFileChooseDefaultDir(), getProperties().getProperty("homeFolderWindows"));
        } else {
            return new File(System.getProperty("user.home"), getProperties().getProperty("homeFolder"));
        }
    }

    private static boolean isPortableMode() {
        return new File("portable.txt").exists();
    }


}
