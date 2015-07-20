/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.buildtools;

import com.beust.jcommander.JCommander;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.InstanceList;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.OfflineSession;
import com.skcraft.launcher.auth.Session;
import com.skcraft.launcher.builder.BuilderConfig;
import com.skcraft.launcher.buildtools.BuildDialog.BuildOptions;
import com.skcraft.launcher.dialog.ConfigurationDialog;
import com.skcraft.launcher.dialog.ConsoleFrame;
import com.skcraft.launcher.dialog.ProgressDialog;
import com.skcraft.launcher.launch.LaunchOptions;
import com.skcraft.launcher.launch.LaunchOptions.UpdatePolicy;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.SwingHelper;
import lombok.extern.java.Log;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

@Log
public class BuildTools {

    private static DateFormat VERSION_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private static Pattern FILENAME_SANITIZE = Pattern.compile("[^a-z0-9_\\-\\.]+");

    private final Launcher launcher;
    private String configFilename = "modpack.json";
    private int port;
    private final File inputDir;
    private final File wwwDir;
    private final File distDir;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public BuildTools(File baseDir, int port) throws IOException {
        File launcherDir = new File(baseDir, "staging/launcher");
        inputDir = baseDir;
        wwwDir = new File(baseDir, "staging/www");
        distDir = new File(baseDir, "upload");

        this.port = port;

        launcherDir.mkdirs();
        wwwDir.mkdirs();

        launcher = new Launcher(launcherDir);
        setPort(port);
    }

    private void setPort(int port) {
        this.port = port;
        launcher.getProperties().setProperty("newsUrl", "http://localhost:" + port + "/news.html");
        launcher.getProperties().setProperty("packageListUrl", "http://localhost:" + port + "/packages.json");
        launcher.getProperties().setProperty("selfUpdateUrl", "http://localhost:" + port + "/latest.json");
    }

    public String generateManifestName() {
        File file = new File(inputDir, configFilename);
        if (file.exists()) {
            BuilderConfig config = Persistence.read(file, BuilderConfig.class, true);
            if (config != null) {
                String name = Strings.nullToEmpty(config.getName());
                name = name.toLowerCase();
                name = FILENAME_SANITIZE.matcher(name).replaceAll("-");
                name = name.trim();
                if (!name.isEmpty()) {
                    return name + ".json";
                }
            }
        }

        return "my_modpack.json";
    }

    public String getCurrentModpackName() {
        File file = new File(inputDir, configFilename);
        if (file.exists()) {
            BuilderConfig config = Persistence.read(file, BuilderConfig.class, true);
            if (config != null) {
                return config.getName();
            }
        }

        return null;
    }

    public Instance findCurrentInstsance(List<Instance> instances) {
        String expected = getCurrentModpackName();

        for (Instance instance : instances) {
            if (instance.getName().equals(expected)) {
                return instance;
            }
        }

        return null;
    }

    public Server startHttpServer() throws Exception {
        LocalHttpServerBuilder builder = new LocalHttpServerBuilder();
        builder.setBaseDir(wwwDir);
        builder.setPort(port);

        Server server = builder.build();
        server.start();
        setPort(((ServerConnector) server.getConnectors()[0]).getLocalPort());
        return server;
    }

    private void showMainWindow() {
        final ToolsFrame frame = new ToolsFrame();

        frame.getBuildButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BuildOptions options = BuildDialog.showBuildDialog(frame, generateVersionFromDate(), generateManifestName());
                if (options != null) {
                    ConsoleFrame.showMessages();

                    distDir.mkdirs();
                    ModpackBuilder runnable = new ModpackBuilder(inputDir, distDir, options.getVersion(), options.getManifestFilename(), options.isClean());
                    ObservableFuture<ModpackBuilder> future = new ObservableFuture<ModpackBuilder>(launcher.getExecutor().submit(runnable), runnable);
                    ProgressDialog.showProgress(frame, future, "Building modpack...", "Building modpack for release...");

                    Futures.addCallback(future, new FutureCallback<ModpackBuilder>() {
                        @Override
                        public void onSuccess(ModpackBuilder result) {
                            SwingHelper.browseDir(distDir, frame);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                        }
                    });

                    SwingHelper.addErrorDialogCallback(frame, future);
                }
            }
        });

        frame.getTestButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConsoleFrame.showMessages();

                ModpackBuilder runnable = new ModpackBuilder(inputDir, wwwDir, generateVersionFromDate(), "staging.json", false);
                ObservableFuture<ModpackBuilder> future = new ObservableFuture<ModpackBuilder>(launcher.getExecutor().submit(runnable), runnable);
                ProgressDialog.showProgress(frame, future, "Preparing files...", "Preparing files for launch...");
                SwingHelper.addErrorDialogCallback(frame, future);

                Futures.addCallback(future, new FutureCallback<ModpackBuilder>() {
                    @Override
                    public void onSuccess(ModpackBuilder result) {
                        launchInstance(frame);
                        ConsoleFrame.hideMessages();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                    }
                });
            }
        });

        frame.getOptionsButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigurationDialog configDialog = new ConfigurationDialog(frame, launcher);
                configDialog.setVisible(true);
            }
        });

        frame.getClearInstanceButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DirectoryRemover remover = new DirectoryRemover(launcher.getInstancesDir());
                ObservableFuture<DirectoryRemover> future = new ObservableFuture<DirectoryRemover>(launcher.getExecutor().submit(remover), remover);
                ProgressDialog.showProgress(frame, future, "Removing files...", "Removing files...");
                SwingHelper.addErrorDialogCallback(frame, future);
            }
        });

        frame.getClearWebRootButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DirectoryRemover remover = new DirectoryRemover(wwwDir);
                ObservableFuture<DirectoryRemover> future = new ObservableFuture<DirectoryRemover>(launcher.getExecutor().submit(remover), remover);
                ProgressDialog.showProgress(frame, future, "Removing files...", "Removing files...");
                SwingHelper.addErrorDialogCallback(frame, future);
            }
        });

        frame.getOpenConsoleButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConsoleFrame.showMessages();
            }
        });

        frame.getDocsButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingHelper.openURL("https://github.com/SKCraft/Launcher/wiki", frame);
            }
        });

        frame.setVisible(true);
    }

    private void launchInstance(final Window window) {
        String expectedName = getCurrentModpackName();

        final InstanceList instanceList = launcher.getInstances();
        InstanceList.Enumerator loader = instanceList.createEnumerator();
        ObservableFuture<InstanceList> future = new ObservableFuture<InstanceList>(launcher.getExecutor().submit(loader), loader);
        ProgressDialog.showProgress(window, future, "Loading modpacks...", "Loading modpacks...");
        SwingHelper.addErrorDialogCallback(window, future);

        Futures.addCallback(future, new FutureCallback<InstanceList>() {
            @Override
            public void onSuccess(InstanceList result) {
                Session session = new OfflineSession("Player");

                Instance instance = findCurrentInstsance(instanceList.getInstances());

                if (instance != null) {
                    LaunchOptions options = new LaunchOptions.Builder()
                            .setInstance(instance)
                            .setUpdatePolicy(UpdatePolicy.ALWAYS_UPDATE)
                            .setWindow(window)
                            .setSession(session)
                            .build();

                    launcher.getLaunchSupervisor().launch(options);
                } else {
                    SwingHelper.showErrorDialog(window,
                            "After generating the necessary files, it appears the modpack can't be found in the " +
                                    "launcher. Did you change modpack.json while the launcher was launching?", "Launch Error");
                }
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    public static void main(String[] args) throws Exception {
        Launcher.setupLogger();
        System.setProperty("skcraftLauncher.killWithoutConfirm", "true");

        ToolArguments options = new ToolArguments();
        new JCommander(options, args);

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                }
            }
        });

        final BuildTools main = new BuildTools(options.getDir(), options.getPort());

        try {
            main.startHttpServer();
        } catch (Throwable t) {
            log.log(Level.WARNING, "Web server start failure", t);
            SwingHelper.showErrorDialog(null, "Couldn't start the local web server on a free TCP port! " +
                    "The web server is required to temporarily host the modpack files for the launcher.", "Build Tools Error", t);
            System.exit(1);
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    main.showMainWindow();
                } catch (Throwable t) {
                    log.log(Level.WARNING, "Load failure", t);
                    SwingHelper.showErrorDialog(null, "Failed to launch build tools!", "Build Tools Error", t);
                }
            }
        });
    }

    public static String generateVersionFromDate() {
        Date today = Calendar.getInstance().getTime();
        return VERSION_DATE_FORMAT.format(today);
    }

}
