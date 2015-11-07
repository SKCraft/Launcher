/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */
package com.skcraft.launcher;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.skcraft.launcher.auth.AccountList;
import com.skcraft.launcher.auth.LoginService;
import com.skcraft.launcher.auth.YggdrasilLoginService;
import com.skcraft.launcher.dialog.LauncherFrame;
import com.skcraft.launcher.launch.LaunchSupervisor;
import com.skcraft.launcher.model.minecraft.VersionManifest;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.HttpRequest;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SimpleLogFormatter;

import com.sun.management.OperatingSystemMXBean;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;
import nz.co.lolnet.statistics.ThreadLauncherIsLaunched;
import org.apache.commons.io.FileUtils;

/**
 * The main entry point for the launcher.
 */
@Log
public final class Launcher {

    public static final boolean debugmode = false;
    public static boolean java8OrAbove = false;
    public static final int PROTOCOL_VERSION = 2;

    @Getter
    private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    @Getter
    private final File baseDir;
    @Getter
    private final Properties properties;
    @Getter
    private final InstanceList instances;
    @Getter
    private final Configuration config;
    @Getter
    private final AccountList accounts;
    @Getter
    private final AssetsRoot assets;
    @Getter
    private final LaunchSupervisor launchSupervisor = new LaunchSupervisor(this);

    public static File dataDir;
    public static File launcherJarFile;

    public static String mainServerURL;
    public static String backupServerURL;

    /**
     * Create a new launcher instance with the given base directory.
     *
     * @param baseDir the base directory
     * @throws java.io.IOException on load error
     */
    public Launcher(@NonNull File baseDir) throws IOException {

        new ThreadLauncherIsLaunched();

        SharedLocale.loadBundle("com.skcraft.launcher.lang.Launcher", Locale.getDefault());

        this.baseDir = baseDir;
        this.properties = LauncherUtils.loadProperties(Launcher.class, "launcher.properties", "com.skcraft.launcher.propertiesFile");
        this.instances = new InstanceList(this);
        this.assets = new AssetsRoot(new File(baseDir, "assets"));
        this.config = Persistence.load(new File(baseDir, "config.json"), Configuration.class);
        config.setupJVMPath();
        config.setupJVMargs();
        config.setupMemory();
        config.checkVaules();
        mainServerURL = getProperties().getProperty("mainServerURL");
        backupServerURL = getProperties().getProperty("backupServerURL");
        this.accounts = Persistence.load(new File(baseDir, "accounts.dat"), AccountList.class);

        setDefaultConfig();

        if (accounts.getSize() > 0) {
            accounts.setSelectedItem(accounts.getElementAt(0));
        }

        executor.submit(new Runnable() {
            @Override
            public void run() {
                cleanupExtractDir();
            }
        });
    }

    public static URL checkURL(URL url) {
        if (true) {
            return url;
        }
        try {
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            if (huc.getResponseCode() == 404) {
                url = new URL(url.toString().replace(Launcher.mainServerURL, Launcher.backupServerURL));
            }
        } catch (IOException ex) {
            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        return url;
    }

    public static boolean restartLauncher() {
        Preferences userNodeForPackage = java.util.prefs.Preferences.userRoot();
        String boot = userNodeForPackage.get("LolnetLauncherbootstrap", "");
        if (boot != null && boot.equals("true")) {
            String bootLocation = userNodeForPackage.get("LolnetLauncherbootstrapLocation", "");
            if (bootLocation.toLowerCase().contains("exe") || bootLocation.toLowerCase().contains("jar")) {
                Runtime rt = Runtime.getRuntime();
                String command = "java -jar " + "\"" + bootLocation + "\"";
                try {
                    Process pr = rt.exec(command);
                    System.exit(0);
                } catch (IOException ex) {

                    Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            } else {
                return false;
            }

        } else {
            String bootLocation;
            try {
                bootLocation = new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getAbsolutePath();
            } catch (URISyntaxException ex) {
                Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            if (bootLocation.toLowerCase().contains("exe") || bootLocation.toLowerCase().contains("jar")) {
                Runtime rt = Runtime.getRuntime();
                String command = "java -jar " + "\"" + bootLocation + "\"";
                try {
                    Process pr = rt.exec(command);
                    System.exit(0);
                } catch (IOException ex) {

                    Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Updates any incorrect / unset configuration settings with defaults.
     */
    public void setDefaultConfig() {
        double configMax = config.getMaxMemory() / 1024.0;
        double suggestedMax = 2;
        double available = Double.MAX_VALUE;

        try {
            OperatingSystemMXBean bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            available = bean.getTotalPhysicalMemorySize() / 1024.0 / 1024.0 / 1024.0;
            if (available <= 6) {
                suggestedMax = available * 0.48;
            } else {
                suggestedMax = 4;
            }
        } catch (Exception ignored) {
        }

        if (config.getMaxMemory() <= 0 || configMax >= available - 1) {
            config.setMaxMemory((int) (suggestedMax * 1024));
        }
    }

    /**
     * Get the launcher version.
     *
     * @return the launcher version
     */
    public String getVersion() {
        String version = getProperties().getProperty("version");
        if (version.equals("${project.version}")) {
            return "1.0.0-SNAPSHOT";
        }
        return version;
    }

    /**
     * Get a login service.
     *
     * @return a login service
     */
    public LoginService getLoginService() {
        return new YggdrasilLoginService(HttpRequest.url(getProperties().getProperty("yggdrasilAuthUrl")));
    }

    /**
     * Get the directory containing the instances.
     *
     * @return the instances dir
     */
    public File getInstancesDir() {
        return new File(getBaseDir(), "instances");
    }

    /**
     * Get the directory to store temporary files.
     *
     * @return the temporary directory
     */
    public File getTemporaryDir() {
        return new File(getBaseDir(), "temp");
    }

    /**
     * Get the directory to store temporary install files.
     *
     * @return the temporary install directory
     */
    public File getInstallerDir() {
        return new File(getTemporaryDir(), "install");
    }

    /**
     * Get the directory to store temporarily extracted files.
     *
     * @return the directory
     */
    private File getExtractDir() {
        return new File(getTemporaryDir(), "extract");
    }

    /**
     * Delete old extracted files.
     */
    public void cleanupExtractDir() {
        log.info("Cleaning up temporary extracted files directory...");

        final long now = System.currentTimeMillis();

        File[] dirs = getExtractDir().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                try {
                    long time = Long.parseLong(pathname.getName());
                    return (now - time) > (1000 * 60 * 60);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });

        if (dirs != null) {
            for (File dir : dirs) {
                log.info("Removing " + dir.getAbsolutePath() + "...");
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    log.log(Level.WARNING, "Failed to delete " + dir.getAbsolutePath(), e);
                }
            }
        }
    }

    /**
     * Create a new temporary directory to extract files to.
     *
     * @return the directory path
     */
    public File createExtractDir() {
        File dir = new File(getExtractDir(), String.valueOf(System.currentTimeMillis()));
        dir.mkdirs();
        log.info("Created temporary directory " + dir.getAbsolutePath());
        return dir;
    }

    /**
     * Get the directory to store the launcher binaries.
     *
     * @return the libraries directory
     */
    public File getLauncherBinariesDir() {
        return new File(getBaseDir(), "launcher");
    }

    /**
     * Get the directory to store common data files.
     *
     * @return the common data directory
     */
    public File getCommonDataDir() {
        return getBaseDir();
    }

    /**
     * Get the directory to store libraries.
     *
     * @return the libraries directory
     */
    public File getLibrariesDir() {
        return new File(getCommonDataDir(), "libraries");
    }

    /**
     * Get the directory to store versions.
     *
     * @return the versions directory
     */
    public File getVersionsDir() {
        return new File(getCommonDataDir(), "versions");
    }

    /**
     * Get the directory to store a version.
     *
     * @param version the version
     * @return the directory
     */
    public File getVersionDir(String version) {
        return new File(getVersionsDir(), version);
    }

    /**
     * Get the path to the JAR for the given version manifest.
     *
     * @param versionManifest the version manifest
     * @return the path
     */
    public File getJarPath(VersionManifest versionManifest) {
        return new File(getVersionDir(versionManifest.getId()), versionManifest.getId() + ".jar");
    }

    /**
     * Get the news URL.
     *
     * @return the news URL
     */
    public URL getNewsURL() {
        try {
            return Launcher.checkURL(HttpRequest.url(
                    String.format(getProperties().getProperty("newsUrl"),
                            URLEncoder.encode(getVersion(), "UTF-8"))));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the packages URL.
     *
     * @return the packages URL
     */
    public URL getPackagesURL() {
        try {
            String key = Strings.nullToEmpty(getConfig().getGameKey());
            return Launcher.checkURL(HttpRequest.url(
                    String.format(getProperties().getProperty("packageListUrl"),
                            URLEncoder.encode(key, "UTF-8"))));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convenient method to fetch a property.
     *
     * @param key the key
     * @return the property
     */
    public String prop(String key) {
        return getProperties().getProperty(key);
    }

    /**
     * Convenient method to fetch a property.
     *
     * @param key the key
     * @param args formatting arguments
     * @return the property
     */
    public String prop(String key, String... args) {
        return String.format(getProperties().getProperty(key), (Object[]) args);
    }

    /**
     * Convenient method to fetch a property.
     *
     * @param key the key
     * @return the property
     */
    public URL propUrl(String key) {
        return HttpRequest.url(prop(key));
    }

    /**
     * Convenient method to fetch a property.
     *
     * @param key the key
     * @param args formatting arguments
     * @return the property
     */
    public URL propUrl(String key, String... args) {
        return HttpRequest.url(prop(key, args));
    }

    /**
     * Bootstrap.
     *
     * @param args args
     */
    private static String defaultDirectory() {
        String OS = System.getProperty("os.name").toUpperCase();
        if (OS.contains("WIN")) {
            return System.getenv("APPDATA");
        } else if (OS.contains("MAC")) {
            return System.getProperty("user.home") + "/Library/Application "
                    + "Support" + "/";
        } else if (OS.contains("NUX")) {
            return System.getProperty("user.home");
        }
        return System.getProperty("user.dir");
    }

    public static void main() {
        String[] args = new String[1];
        main(args);
    }

    public static void main(String[] args) {

        SimpleLogFormatter.configureGlobalLogger();
        launcherJarFile = new java.io.File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        Preferences userNodeForPackage = java.util.prefs.Preferences.userRoot();
        String property = System.getProperty("java.version");

        if (!property.startsWith("1.5") && !property.startsWith("1.6") && !property.startsWith("1.7")) {
            java8OrAbove = true;
            Configuration.setImplicitExit();
        }
        LauncherArguments options = new LauncherArguments();
        try {
            new JCommander(options, args);
        } catch (ParameterException e) {
            System.err.print(e.getMessage());
            System.exit(1);
            return;
        }

        Integer bsVersion = options.getBootstrapVersion();
        log.info(bsVersion != null ? "Bootstrap version " + bsVersion + " detected" : "Not bootstrapped");
        String currentDataPath = userNodeForPackage.get("LolnetLauncherDataPath", "");
        if (currentDataPath == null || currentDataPath.equalsIgnoreCase("")) {
            currentDataPath = defaultDirectory() + File.separator + "LolnetData/";
        }
        Launcher.dataDir = new File(currentDataPath);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        if (!Launcher.dataDir.exists()) {
            Launcher.dataDir = new File(defaultDirectory() + File.separator + "LolnetData/");
        }
        if (dataDir != null) {
            log.info("Using given base directory " + dataDir.getAbsolutePath());
        } else {
            dataDir = new File(".");
            log.info("Using current directory " + dataDir.getAbsolutePath());
        }

        final File baseDir = dataDir;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                    UIManager.getDefaults().put("SplitPane.border", BorderFactory.createEmptyBorder());
                    Launcher launcher = new Launcher(baseDir);
                    new LauncherFrame(launcher).setVisible(true);
                } catch (Throwable t) {
                    log.log(Level.WARNING, "Load failure", t);
                    SwingHelper.showErrorDialog(null, "Uh oh! The updater couldn't be opened because a "
                            + "problem was encountered.", "Launcher error", t);
                }
            }
        });

    }

}
