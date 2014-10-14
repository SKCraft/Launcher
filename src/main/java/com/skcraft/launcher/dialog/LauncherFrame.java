/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */
package com.skcraft.launcher.dialog;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.InstanceList;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.Session;
import com.skcraft.launcher.launch.LaunchProcessHandler;
import com.skcraft.launcher.launch.Runner;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.selfupdate.SelfUpdater;
import com.skcraft.launcher.selfupdate.UpdateChecker;
import com.skcraft.launcher.swing.*;
import com.skcraft.launcher.update.HardResetter;
import com.skcraft.launcher.update.Remover;
import com.skcraft.launcher.update.Updater;
import static com.skcraft.launcher.util.SharedLocale._;
import com.skcraft.launcher.util.SwingExecutor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import lombok.NonNull;
import lombok.extern.java.Log;
import nz.co.lolnet.james137137.PrivatePrivatePackagesManager;
import nz.co.lolnet.james137137.ThreadLolnetPingWindow;
import org.apache.commons.io.FileUtils;

/**
 * The main launcher frame.
 */
@Log
public class LauncherFrame extends JFrame {

    private final Launcher launcher;

    private final HeaderPanel header = new HeaderPanel();
    private final InstanceTable instancesTable = new InstanceTable();
    private final InstanceTableModel instancesModel;
    private final JScrollPane instanceScroll = new JScrollPane(instancesTable);
    private WebpagePanel webView;
    private JSplitPane splitPane;
    private final JPanel container = new JPanel();
    private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true).fullyPadded();
    private final JButton launchButton = new JButton(_("launcher.launch"));
    private final JButton lolnetPingButton = new JButton("Check Servers...");
    private final JButton lolnetPrivatePackButton = new JButton("Private Pack...");
    private final JButton refreshButton = new JButton(_("launcher.checkForUpdates"));
    private final JButton optionsButton = new JButton(_("launcher.options"));
    private final JButton selfUpdateButton = new JButton(_("launcher.updateLauncher"));
    private final JCheckBox updateCheck = new JCheckBox(_("launcher.downloadUpdates"));
    private URL updateUrl;

    /**
     * Create a new frame.
     *
     * @param launcher the launcher
     */
    public LauncherFrame(@NonNull Launcher launcher) {
        super(_("launcher.title", launcher.getVersion()));

        this.launcher = launcher;
        instancesModel = new InstanceTableModel(launcher.getInstances());

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(850, 550);
        setMinimumSize(new Dimension(400, 300));
        initComponents();
        setLocationRelativeTo(null);

        SwingHelper.setIconImage(this, Launcher.class, "icon.png");

        loadInstances();
        checkLauncherUpdate();
    }

    private void initComponents() {
        webView = WebpagePanel.forURL(launcher.getNewsURL(), false);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, instanceScroll, webView);
        selfUpdateButton.setVisible(false);

        updateCheck.setSelected(true);
        instancesTable.setModel(instancesModel);
        launchButton.setFont(launchButton.getFont().deriveFont(Font.BOLD));
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(4);
        SwingHelper.flattenJSplitPane(splitPane);
        buttonsPanel.addElement(refreshButton);
        buttonsPanel.addElement(updateCheck);
        buttonsPanel.addGlue();
        buttonsPanel.addElement(selfUpdateButton);
        buttonsPanel.addElement(lolnetPrivatePackButton);
        buttonsPanel.addElement(lolnetPingButton);
        buttonsPanel.addElement(optionsButton);
        buttonsPanel.addElement(launchButton);
        container.setLayout(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        container.add(splitPane, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
        add(container, BorderLayout.CENTER);

        instancesModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (instancesTable.getRowCount() > 0) {
                    instancesTable.setRowSelectionInterval(0, 0);
                }
            }
        });

        instancesTable.addMouseListener(new DoubleClickToButtonAdapter(launchButton));

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadInstances();
                checkLauncherUpdate();
            }
        });

        selfUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selfUpdate();
            }
        });

        optionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOptions();
            }
        });

        lolnetPrivatePackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchPrivatePackPannel();
            }

        });

        lolnetPingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPingServer();
            }

        });

        launchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launch();
            }
        });

        instancesTable.addMouseListener(new PopupMouseAdapter() {
            @Override
            protected void showPopup(MouseEvent e) {
                int index = instancesTable.rowAtPoint(e.getPoint());
                Instance selected = null;
                if (index >= 0) {
                    instancesTable.setRowSelectionInterval(index, index);
                    selected = launcher.getInstances().get(index);
                }
                popupInstanceMenu(e.getComponent(), e.getX(), e.getY(), selected);
            }
        });
    }

    private void checkLauncherUpdate() {
        if (SelfUpdater.updatedAlready) {
            return;
        }

        ListenableFuture<URL> future = launcher.getExecutor().submit(new UpdateChecker(launcher));

        Futures.addCallback(future, new FutureCallback<URL>() {
            @Override
            public void onSuccess(URL result) {
                if (result != null) {
                    requestUpdate(result);
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        }, SwingExecutor.INSTANCE);
    }

    private void selfUpdate() {
        URL url = updateUrl;
        if (Launcher.launcherJarFile.getName().contains(".exe")) {
            try {
                url = new URL(url.toString().replaceAll(".jar", ".exe"));
            } catch (MalformedURLException ex) {
                Logger.getLogger(LauncherFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (url != null) {
            SelfUpdater downloader = new SelfUpdater(launcher, url);
            ObservableFuture<File> future = new ObservableFuture<File>(
                    launcher.getExecutor().submit(downloader), downloader);

            Futures.addCallback(future, new FutureCallback<File>() {
                @Override
                public void onSuccess(File result) {
                    selfUpdateButton.setVisible(false);
                    SwingHelper.showMessageDialog(
                            LauncherFrame.this,
                            _("launcher.selfUpdateComplete"),
                            _("launcher.selfUpdateCompleteTitle"),
                            null,
                            JOptionPane.INFORMATION_MESSAGE);
                    if (Launcher.launcherJarFile.getName().contains(".jar") || Launcher.launcherJarFile.getName().contains(".exe")) {
                        if (JOptionPane.showConfirmDialog(null, "Would you like to restart now?", "Restart?",
                                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            try {
                                Runtime rt = Runtime.getRuntime();
                                File file = new File(Launcher.launcherJarFile.getAbsolutePath());
                                String path = file.getAbsolutePath().replaceAll("%20", " ");
                                String command = "java -jar " + "\"" + path + "\"".replaceAll("%20", " ");
                                Process pr = rt.exec(command);
                                System.out.println(command);
                                // yes option
                            } catch (IOException ex) {
                                Logger.getLogger(ConfigurationDialog.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            System.exit(0);
                        } else {
                            // no option
                        }
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                }
            }, SwingExecutor.INSTANCE);

            ProgressDialog.showProgress(this, future, _("launcher.selfUpdatingTitle"), _("launcher.selfUpdatingStatus"));
            SwingHelper.addErrorDialogCallback(this, future);
        } else {
            selfUpdateButton.setVisible(false);
        }
    }

    private void requestUpdate(URL url) {
        this.updateUrl = url;
        if (JOptionPane.showConfirmDialog(null, "Launcher has found an update\n\nDo you wish to update?", "Launcher Update Available",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            selfUpdate();
        } else {
            // no option
        }
    }

    /**
     * Popup the menu for the instances.
     *
     * @param component the component
     * @param x mouse X
     * @param y mouse Y
     * @param selected the selected instance, possibly null
     */
    private void popupInstanceMenu(Component component, int x, int y, final Instance selected) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem;

        if (selected != null) {
            menuItem = new JMenuItem(!selected.isLocal() ? "Install" : "Launch");
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    launch();
                }
            });
            popup.add(menuItem);

            if (selected.isLocal()) {
                popup.addSeparator();

                menuItem = new JMenuItem(_("instance.openFolder"));
                menuItem.addActionListener(ActionListeners.browseDir(
                        LauncherFrame.this, selected.getContentDir(), true));
                popup.add(menuItem);

                menuItem = new JMenuItem(_("instance.openSaves"));
                menuItem.addActionListener(ActionListeners.browseDir(
                        LauncherFrame.this, new File(selected.getContentDir(), "saves"), true));
                popup.add(menuItem);

                menuItem = new JMenuItem(_("instance.openResourcePacks"));
                menuItem.addActionListener(ActionListeners.browseDir(
                        LauncherFrame.this, new File(selected.getContentDir(), "resourcepacks"), true));
                popup.add(menuItem);

                menuItem = new JMenuItem(_("instance.openScreenshots"));
                menuItem.addActionListener(ActionListeners.browseDir(
                        LauncherFrame.this, new File(selected.getContentDir(), "screenshots"), true));
                popup.add(menuItem);

                menuItem = new JMenuItem(_("instance.copyAsPath"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        File dir = selected.getContentDir();
                        dir.mkdirs();
                        SwingHelper.setClipboard(dir.getAbsolutePath());
                    }
                });
                popup.add(menuItem);

                popup.addSeparator();

                if (!selected.isUpdatePending()) {
                    menuItem = new JMenuItem(_("instance.forceUpdate"));
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            selected.setUpdatePending(true);
                            launch();
                            instancesModel.update();
                        }
                    });
                    popup.add(menuItem);
                }

                menuItem = new JMenuItem(_("instance.hardForceUpdate"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        confirmHardUpdate(selected);
                    }
                });
                popup.add(menuItem);

                menuItem = new JMenuItem(_("instance.deleteFiles"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        confirmDelete(selected);
                    }
                });
                popup.add(menuItem);
            }

            popup.addSeparator();
        }

        menuItem = new JMenuItem(_("launcher.refreshList"));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadInstances();
            }
        });
        popup.add(menuItem);

        popup.show(component, x, y);

    }

    private void confirmDelete(Instance instance) {
        if (!SwingHelper.confirmDialog(this,
                _("instance.confirmDelete", instance.getTitle()), _("confirmTitle"))) {
            return;
        }

        // Execute the deleter
        Remover resetter = new Remover(instance);
        ObservableFuture<Instance> future = new ObservableFuture<Instance>(
                launcher.getExecutor().submit(resetter), resetter);

        // Show progress
        ProgressDialog.showProgress(
                this, future, _("instance.deletingTitle"), _("instance.deletingStatus", instance.getTitle()));
        SwingHelper.addErrorDialogCallback(this, future);

        // Update the list of instances after updating
        future.addListener(new Runnable() {
            @Override
            public void run() {
                loadInstances();
            }
        }, SwingExecutor.INSTANCE);
    }

    private void confirmHardUpdate(Instance instance) {
        if (!SwingHelper.confirmDialog(this, _("instance.confirmHardUpdate"), _("confirmTitle"))) {
            return;
        }

        // Execute the resetter
        HardResetter resetter = new HardResetter(instance);
        ObservableFuture<Instance> future = new ObservableFuture<Instance>(
                launcher.getExecutor().submit(resetter), resetter);

        // Show progress
        ProgressDialog.showProgress(this, future, _("instance.resettingTitle"),
                _("instance.resettingStatus", instance.getTitle()));
        SwingHelper.addErrorDialogCallback(this, future);

        // Update the list of instances after updating
        future.addListener(new Runnable() {
            @Override
            public void run() {
                launch();
                instancesModel.update();
            }
        }, SwingExecutor.INSTANCE);
    }

    private void loadInstances() {
        InstanceList.Enumerator loader = launcher.getInstances().createEnumerator();
        ObservableFuture<InstanceList> future = new ObservableFuture<InstanceList>(
                launcher.getExecutor().submit(loader), loader);

        future.addListener(new Runnable() {
            @Override
            public void run() {
                instancesModel.update();
                if (instancesTable.getRowCount() > 0) {
                    instancesTable.setRowSelectionInterval(0, 0);
                }
                requestFocus();
            }
        }, SwingExecutor.INSTANCE);

        ProgressDialog.showProgress(this, future, _("launcher.checkingTitle"), _("launcher.checkingStatus"));
        SwingHelper.addErrorDialogCallback(this, future);
    }

    private void showOptions() {
        ConfigurationDialog configDialog = new ConfigurationDialog(this, launcher);
        configDialog.setVisible(true);
    }

    private void showPingServer() {
        new ThreadLolnetPingWindow();
    }

    private void launchPrivatePackPannel() {
        LinedBoxPanel pPButtonsPanel = new LinedBoxPanel(true).fullyPadded();
        JFrame frame = new JFrame("Private Pack Code here");
        JButton pPAddButton = new JButton("Add");
        JButton pPCloseButton = new JButton("Close");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(new Dimension(300, 150));
        frame.setResizable(false);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
        JTextField field = new JTextField();
        field.setBorder(BorderFactory.createLineBorder(Color.black));
        final JTextArea area = new JTextArea(100, 80);
        JScrollPane scrollPane = new JScrollPane(area);
        area.setEditable(true);
        pPButtonsPanel.addGlue();
        pPButtonsPanel.add(pPAddButton, BorderLayout.EAST);
        pPButtonsPanel.add(pPCloseButton, BorderLayout.EAST);

        frame.add(pPButtonsPanel, BorderLayout.SOUTH);
        frame.add(scrollPane);
        frame.setVisible(true);

        pPCloseButton.addActionListener(ActionListeners.dispose(frame));

        pPAddButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String text = area.getText();
                String[] split = text.split("\n");
                text = "";
                BufferedReader br;
                boolean newPackAdded = false;
                for (int i = 0; i < split.length; i++) {
                    String code = split[i];
                    boolean alreadyAdded = false;
                    if (code.equalsIgnoreCase("showmethemoney")) {

                        /*lolnetPingButton.setVisible(true);
                         try {
                         File codeFile = new File(PrivatePrivatePackagesManager.dir, "codes.txt");
                         if (!codeFile.exists()) {
                         codeFile.createNewFile();
                         }
                         br = new BufferedReader(new FileReader(codeFile));
                         for (String line; (line = br.readLine()) != null;) {
                         if (line.equalsIgnoreCase("showmethemoney")) {
                         alreadyAdded = true;
                         }
                         }
                         if (!alreadyAdded)
                         {
                         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(codeFile, true)));
                         out.println(code);
                         out.close();
                         split[i] = "Done";
                         }
                        
                         } catch (Exception e1) {
                         split[i] = "error";
                         }*/
                    } else {

                        try {
                            File codeFile = new File(PrivatePrivatePackagesManager.dir, "codes.txt");
                            if (!codeFile.exists()) {
                                codeFile.createNewFile();
                            }
                            br = new BufferedReader(new FileReader(codeFile));
                            for (String line; (line = br.readLine()) != null;) {
                                if (line.startsWith("lolnet:")) {
                                    if (line.split(":")[1].equalsIgnoreCase(code.replaceAll(" ", ""))) {
                                        alreadyAdded = true;
                                    }
                                }
                            }

                            if (!alreadyAdded) {
                                URL oracle = new URL("https://www.lolnet.co.nz/modpack/private/" + code + ".json" + "?key=%s");
                                BufferedReader in = new BufferedReader(
                                        new InputStreamReader(oracle.openStream()));

                                String inputLine;
                                while ((inputLine = in.readLine()) != null) {
                                    if (inputLine.length() >= 1) {
                                        break;
                                    }
                                }
                                in.close();

                                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(codeFile, true)));
                                out.println("lolnet:" + code);
                                out.close();
                                split[i] = "Done";
                                newPackAdded = true;
                            } else {
                                split[i] = "alreadyAdded";
                            }
                        } catch (IOException ex) {
                            split[i] = "WrongCode";
                        }
                    }
                    text += split[i] + "\n";
                }
                if (newPackAdded) {
                    loadInstances();
                }
                area.setText(text);
            }
        });
    }

    private void launch() {
        try {
            final Instance instance = launcher.getInstances().get(instancesTable.getSelectedRow());
            boolean update = updateCheck.isSelected() && instance.isUpdatePending();

            // Store last access date
            Date now = new Date();
            instance.setLastAccessed(now);
            Persistence.commitAndForget(instance);

            // Perform login
            final Session session = LoginDialog.showLoginRequest(this, launcher);
            if (session == null) {
                return;
            }

            // If we have to update, we have to update
            if (!instance.isInstalled()) {
                update = true;
            }

            if (update) {
                // Execute the updater
                Updater updater = new Updater(launcher, instance);
                updater.setOnline(session.isOnline());
                ObservableFuture<Instance> future = new ObservableFuture<Instance>(
                        launcher.getExecutor().submit(updater), updater);

                // Show progress
                ProgressDialog.showProgress(
                        this, future, _("launcher.updatingTitle"), _("launcher.updatingStatus", instance.getTitle()));
                SwingHelper.addErrorDialogCallback(this, future);

                // Update the list of instances after updating
                future.addListener(new Runnable() {
                    @Override
                    public void run() {
                        instancesModel.update();
                    }
                }, SwingExecutor.INSTANCE);

                // On success, launch also
                Futures.addCallback(future, new FutureCallback<Instance>() {
                    @Override
                    public void onSuccess(Instance result) {
                        launch(instance, session);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                    }
                }, SwingExecutor.INSTANCE);
            } else {
                launch(instance, session);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            SwingHelper.showErrorDialog(this, _("launcher.noInstanceError"), _("launcher.noInstanceTitle"));
        }
    }

    private void launch(Instance instance, Session session) {
        final File extractDir = launcher.createExtractDir();

        // Get the process
        Runner task = new Runner(launcher, instance, session, extractDir);
        ObservableFuture<Process> processFuture = new ObservableFuture<Process>(
                launcher.getExecutor().submit(task), task);

        // Show process for the process retrieval
        ProgressDialog.showProgress(
                this, processFuture, _("launcher.launchingTItle"), _("launcher.launchingStatus", instance.getTitle()));

        // If the process is started, get rid of this window
        Futures.addCallback(processFuture, new FutureCallback<Process>() {
            @Override
            public void onSuccess(Process result) {
                dispose();
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });

        // Watch the created process
        ListenableFuture<?> future = Futures.transform(
                processFuture, new LaunchProcessHandler(launcher), launcher.getExecutor());
        SwingHelper.addErrorDialogCallback(null, future);

        // Clean up at the very end
        future.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    log.info("Process ended; cleaning up " + extractDir.getAbsolutePath());
                    FileUtils.deleteDirectory(extractDir);
                } catch (IOException e) {
                    log.log(Level.WARNING, "Failed to clean up " + extractDir.getAbsolutePath(), e);
                }
                instancesModel.update();
            }
        }, sameThreadExecutor());
    }

}
