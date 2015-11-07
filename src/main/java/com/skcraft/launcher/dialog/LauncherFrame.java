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
import com.skcraft.launcher.Configuration;
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
import com.skcraft.launcher.util.SharedLocale;

import com.skcraft.launcher.util.SwingExecutor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import lombok.NonNull;
import lombok.extern.java.Log;
import nz.co.lolnet.james137137.HelpAndSupport;
import nz.co.lolnet.james137137.SimpleSwingBrowser;
import nz.co.lolnet.james137137.ThreadLolnetPingWindow;
import org.apache.commons.io.FileUtils;

/**
 * The main launcher frame.
 */
@Log
public class LauncherFrame extends JFrame {

    private final Launcher launcher;
    private final InstanceTable instancesTable = new InstanceTable();
    private final InstanceTableModel instancesModel;
    private final JScrollPane instanceScroll = new JScrollPane(instancesTable);
    private JSplitPane splitPane;
    private final JPanel container = new JPanel();
    private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true).fullyPadded();
    private final JButton launchButton = new JButton(SharedLocale.tr("launcher.launch"));
    public static final JButton lolnetPingButton = new JButton("Check Servers...");
    private final JButton lolnetAddCodeButton = new JButton("Add code...");
    private final JButton lolnetModPackButton = new JButton("ModPacks"); // = getButtonFromImage("button1");
    private final JButton lolnetNewsButton = new JButton("News");
    private final JButton lolnetForumButton = new JButton("Forum");
    private final JButton lolnetWikiButton = new JButton("Wiki");
    private final JButton launcherHelpButton = new JButton("Help");

    private final JButton lolnetPublicPackListButton = new JButton("Lolnet Packs");
    private final JButton refreshButton = new JButton(SharedLocale.tr("launcher.checkForUpdates"));
    private final JButton optionsButton = new JButton(SharedLocale.tr("launcher.options"));
    private final JButton selfUpdateButton = new JButton(SharedLocale.tr("launcher.updateLauncher"));
    private final JCheckBox updateCheck = new JCheckBox(SharedLocale.tr("launcher.downloadUpdates"));
    private final JTextField txtURL = new JTextField();
    private final JButton btnGo = new JButton("Go");
    private URL updateUrl;

    /**
     * Create a new frame.
     *
     * @param launcher the launcher
     */
    public LauncherFrame(@NonNull Launcher launcher) {
        super(SharedLocale.tr("launcher.title", launcher.getVersion()));

        this.launcher = launcher;
        instancesModel = new InstanceTableModel(launcher.getInstances());

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //setSize(850, 550);

        Configuration config = launcher.getConfig();
        if (config.getLauncherWindowHeight() < 0 || config.getLauncherWindowWidth() < 0) {
            setSize(Toolkit.getDefaultToolkit().getScreenSize().width - Toolkit.getDefaultToolkit().getScreenSize().width / 6, Toolkit.getDefaultToolkit().getScreenSize().height - Toolkit.getDefaultToolkit().getScreenSize().height / 6);
            setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        } else {
            setSize(config.getLauncherWindowWidth(), config.getLauncherWindowHeight());
            setExtendedState(getExtendedState() | config.getLauncherExtendedState());
        }
        setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - getSize().width / 2 - 50, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - getSize().height / 2);
        setMinimumSize(new Dimension(400, 300));
        initComponents();
        setLocationRelativeTo(null);

        SwingHelper.setIconImage(this, Launcher.class, "icon.png");

        loadInstances(true);
        checkLauncherUpdate();
    }

    private void initComponents() {

        Preferences userNodeForPackage = java.util.prefs.Preferences.userNodeForPackage(Launcher.class);
        if (Launcher.java8OrAbove) {
            SimpleSwingBrowser simpleSwingBrowser = new SimpleSwingBrowser(txtURL);
            simpleSwingBrowser.loadURL(launcher.getNewsURL().toString());
            splitPane = simpleSwingBrowser.splitPane(instanceScroll);
        } else {
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, instanceScroll, null);
        }

        selfUpdateButton.setVisible(false);

        updateCheck.setSelected(true);
        updateCheck.setVisible(false);
        refreshButton.setVisible(false);
        lolnetPingButton.setVisible(false);
        instancesTable.setModel(instancesModel);
        launchButton.setFont(launchButton.getFont().deriveFont(Font.BOLD));
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(4);
        SwingHelper.flattenJSplitPane(splitPane);
        buttonsPanel.addElement(lolnetPublicPackListButton);
        buttonsPanel.addElement(launcherHelpButton);
        buttonsPanel.addElement(lolnetAddCodeButton);
        buttonsPanel.addElement(refreshButton);
        buttonsPanel.addElement(updateCheck);
        if (launcher.java8OrAbove) {
            buttonsPanel.addGlue();
            buttonsPanel.addElement(lolnetModPackButton);
            buttonsPanel.addElement(lolnetNewsButton);
            buttonsPanel.addElement(lolnetForumButton);
            buttonsPanel.addElement(lolnetWikiButton);
        }

        buttonsPanel.addGlue();
        buttonsPanel.addElement(selfUpdateButton);
        buttonsPanel.addElement(lolnetPingButton);
        buttonsPanel.addElement(optionsButton);
        buttonsPanel.addElement(launchButton);

        container.setLayout(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        container.add(splitPane, BorderLayout.CENTER);

        JPanel topBar = new JPanel(new BorderLayout(5, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        topBar.add(txtURL, BorderLayout.CENTER);
        topBar.add(btnGo, BorderLayout.EAST);
        String showURLBar = userNodeForPackage.get("IWantToGoPlaces", "");
        if (showURLBar != null && showURLBar.equals("true")) {
            add(topBar, BorderLayout.NORTH);
        }
        topBar.setVisible(Launcher.java8OrAbove);

        add(buttonsPanel, BorderLayout.SOUTH);
        add(container, BorderLayout.CENTER);

        launcherHelpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HelpAndSupport.Start();
            }
        });
        if (Launcher.java8OrAbove) {
            btnGo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SimpleSwingBrowser.loadURL(txtURL.getText());
                }
            });

            txtURL.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        SimpleSwingBrowser.loadURL(txtURL.getText());
                    }
                }

            });

            instancesTable.addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    final Instance selected = launcher.getInstances().get(instancesTable.getSelectedRow());
                    try {
                        URL url = new URL("https://www.lolnet.co.nz/modpack/news/" + selected.getName().replaceAll(" ", "_") + "/index.html");
                        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                        int responseCode = huc.getResponseCode();
                        if (responseCode != 404) {
                            SimpleSwingBrowser.loadURL(url.toString());
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(LauncherFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }
            });
            lolnetNewsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (instanceScroll.isVisible()) {
                        SimpleSwingBrowser.loadURL(launcher.getNewsURL().toString());
                        splitPane.remove(instanceScroll);
                        instanceScroll.setVisible(false);
                    } else {
                        SimpleSwingBrowser.loadURL(launcher.getNewsURL().toString());
                    }
                }
            });

            lolnetForumButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (instanceScroll.isVisible()) {
                        SimpleSwingBrowser.loadURL("https://www.lolnet.co.nz/forum");
                        splitPane.remove(instanceScroll);
                        instanceScroll.setVisible(false);
                    } else {
                        SimpleSwingBrowser.loadURL("https://www.lolnet.co.nz/forum");
                    }
                }
            });

            lolnetWikiButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (instanceScroll.isVisible()) {
                        SimpleSwingBrowser.loadURL("http://wiki.lolnet.co.nz/");
                        splitPane.remove(instanceScroll);
                        instanceScroll.setVisible(false);
                    } else {
                        SimpleSwingBrowser.loadURL("http://wiki.lolnet.co.nz/");
                    }
                }
            });
        }

        instancesModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (instancesTable.getRowCount() > 0) {
                    instancesTable.setRowSelectionInterval(0, 0);
                }
            }
        });

        instancesTable.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    launch();
                }
            }

        });

        instancesTable.addMouseListener(new DoubleClickToButtonAdapter(launchButton));

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadInstances(true);
                checkLauncherUpdate();
            }
        });

        lolnetModPackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showModPackInstances();
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

        lolnetAddCodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showModPackInstances();
                launchPrivatePackPannel();
            }

        });

        lolnetPublicPackListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                showModPackInstances();

                if (lolnetPublicPackListButton.getText().equals("Lolnet Packs")) {
                    lolnetPublicPackListButton.setText("Loading Private Packs..");
                    InstanceList.showPublic = false;
                    InstanceList.showPrivate = true;
                    loadInstances(false);
                    checkLauncherUpdate();
                } else if (lolnetPublicPackListButton.getText().equals("Private Packs")) {
                    lolnetPublicPackListButton.setText("Loading Every Pack..");
                    InstanceList.showPublic = true;
                    InstanceList.showPrivate = true;
                    loadInstances(false);
                    checkLauncherUpdate();
                } else {
                    lolnetPublicPackListButton.setText("Loading Lolnet Packs..");
                    InstanceList.showPublic = true;
                    InstanceList.showPrivate = false;
                    loadInstances(false);
                    checkLauncherUpdate();

                }

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

    private void showModPackInstances() {
        if (instanceScroll.isVisible()) {

            splitPane.setDividerLocation(250);
        } else {
            splitPane.add(instanceScroll);
            splitPane.setDividerLocation(250);
            instanceScroll.setVisible(true);
            if (Launcher.java8OrAbove) {
                SimpleSwingBrowser.loadURL(launcher.getNewsURL().toString());
            }
        }
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
                            SharedLocale.tr("launcher.selfUpdateComplete"),
                            SharedLocale.tr("launcher.selfUpdateCompleteTitle"),
                            null,
                            JOptionPane.INFORMATION_MESSAGE);
                }

                @Override
                public void onFailure(Throwable t) {
                }
            }, SwingExecutor.INSTANCE);

            ProgressDialog.showProgress(this, future, SharedLocale.tr("launcher.selfUpdatingTitle"), SharedLocale.tr("launcher.selfUpdatingStatus"));
            SwingHelper.addErrorDialogCallback(this, future);
        } else {
            selfUpdateButton.setVisible(false);
        }
    }

    public void requestUpdate(URL url) {
        this.updateUrl = url;
        if (JOptionPane.showConfirmDialog(null, "Launcher has found an update (Version: " + UpdateChecker.latestVersion + ")\n\nDo you wish to update?", "Launcher Update Available",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Preferences userNodeForPackage = java.util.prefs.Preferences.userRoot();
            String bootstrap = userNodeForPackage.get("LolnetLauncherbootstrap", "");
            if (bootstrap == null || bootstrap.equals("")) {
                try {
                    url = new URL("http://anderson.lolnet.co.nz:8081/job/LolnetLauncherBootstrap/lastSuccessfulBuild/artifact/target/LolnetLauncher.jar");
                } catch (MalformedURLException ex) {
                    Logger.getLogger(LauncherFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (Launcher.launcherJarFile.getName().contains(".exe")) {
                    try {
                        url = new URL(url.toString().replaceAll(".jar", ".exe"));
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(LauncherFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    Desktop.getDesktop().browse(url.toURI());
                } catch (IOException | URISyntaxException ex) {
                    Logger.getLogger(LauncherFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            selfUpdate();

            if (JOptionPane.showConfirmDialog(null, "Would you like to restart now?", "Restart?",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                Launcher.restartLauncher();
            }

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

                menuItem = new JMenuItem(SharedLocale.tr("instance.openFolder"));
                menuItem.addActionListener(ActionListeners.browseDir(
                        LauncherFrame.this, selected.getContentDir(), true));
                popup.add(menuItem);

                menuItem = new JMenuItem(SharedLocale.tr("instance.openSaves"));
                menuItem.addActionListener(ActionListeners.browseDir(
                        LauncherFrame.this, new File(selected.getContentDir(), "saves"), true));
                popup.add(menuItem);

                menuItem = new JMenuItem(SharedLocale.tr("instance.openResourcePacks"));
                menuItem.addActionListener(ActionListeners.browseDir(
                        LauncherFrame.this, new File(selected.getContentDir(), "resourcepacks"), true));
                popup.add(menuItem);

                menuItem = new JMenuItem(SharedLocale.tr("instance.openScreenshots"));
                menuItem.addActionListener(ActionListeners.browseDir(
                        LauncherFrame.this, new File(selected.getContentDir(), "screenshots"), true));
                popup.add(menuItem);

                menuItem = new JMenuItem(SharedLocale.tr("instance.copyAsPath"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        File dir = selected.getContentDir();
                        dir.mkdirs();
                        SwingHelper.setClipboard(dir.getAbsolutePath());
                    }
                });
                popup.add(menuItem);

                menuItem = new JMenuItem("View Change Log");
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            URL url = new URL("https://www.lolnet.co.nz/modpack/changelog/" + selected.getName().replaceAll(" ", "_") + ".html");
                            url = Launcher.checkURL(url);
                            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                            int responseCode = huc.getResponseCode();
                            if (responseCode != 404) {
                                if (Launcher.java8OrAbove) {
                                    SimpleSwingBrowser.loadURL(url.toString());
                                } else {
                                    SwingHelper.openURL(url.toString(), rootPane);
                                }
                            } else {
                                if (Launcher.java8OrAbove) {
                                    SimpleSwingBrowser.loadURL(Launcher.checkURL(
                                            new URL("https://www.lolnet.co.nz/modpack/changelog/noChngeLogExist.html")).toString());
                                } else {
                                    SwingHelper.openURL(Launcher.checkURL(
                                            new URL("https://www.lolnet.co.nz/modpack/changelog/noChngeLogExist.html")).toString(), rootPane);
                                }

                            }

                        } catch (IOException ex) {
                            Logger.getLogger(LauncherFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                popup.add(menuItem);

                popup.addSeparator();

                if (!selected.isUpdatePending()) {
                    menuItem = new JMenuItem(SharedLocale.tr("instance.forceUpdate"));
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

                menuItem = new JMenuItem(SharedLocale.tr("instance.hardForceUpdate"));
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        confirmHardUpdate(selected);
                    }
                });
                popup.add(menuItem);

                menuItem = new JMenuItem(SharedLocale.tr("instance.deleteFiles"));
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

        menuItem = new JMenuItem(SharedLocale.tr("launcher.refreshList"));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadInstances(true);
            }
        });
        popup.add(menuItem);

        popup.show(component, x, y);

    }

    private void confirmDelete(Instance instance) {
        if (!SwingHelper.confirmDialog(this,
                SharedLocale.tr("instance.confirmDelete", instance.getTitle()), SharedLocale.tr("confirmTitle"))) {
            return;
        }

        // Execute the deleter
        Remover resetter = new Remover(instance);
        ObservableFuture<Instance> future = new ObservableFuture<Instance>(
                launcher.getExecutor().submit(resetter), resetter);

        // Show progress
        ProgressDialog.showProgress(
                this, future, SharedLocale.tr("instance.deletingTitle"), SharedLocale.tr("instance.deletingStatus", instance.getTitle()));
        SwingHelper.addErrorDialogCallback(this, future);

        // Update the list of instances after updating
        future.addListener(new Runnable() {
            @Override
            public void run() {
                loadInstances(true);
            }
        }, SwingExecutor.INSTANCE);
    }

    private void confirmHardUpdate(Instance instance) {
        if (!SwingHelper.confirmDialog(this, SharedLocale.tr("instance.confirmHardUpdate"), SharedLocale.tr("confirmTitle"))) {
            return;
        }

        // Execute the resetter
        HardResetter resetter = new HardResetter(instance);
        ObservableFuture<Instance> future = new ObservableFuture<Instance>(
                launcher.getExecutor().submit(resetter), resetter);

        // Show progress
        ProgressDialog.showProgress(this, future, SharedLocale.tr("instance.resettingTitle"),
                SharedLocale.tr("instance.resettingStatus", instance.getTitle()));
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

    private void loadInstances(final boolean showProgress) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                loadInstancesThreaded(showProgress);
            }

        });
    }

    private void loadInstancesThreaded(boolean showProgress) {
        if (Launcher.java8OrAbove) {
            SimpleSwingBrowser.loadURL(launcher.getNewsURL().toString());
        }
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
                if (InstanceList.showPublic && InstanceList.showPrivate) {
                    lolnetPublicPackListButton.setText("All Packs");
                } else if (InstanceList.showPublic && !InstanceList.showPrivate) {
                    lolnetPublicPackListButton.setText("Lolnet Packs");
                } else {
                    lolnetPublicPackListButton.setText("Private Packs");
                }

            }
        }, SwingExecutor.INSTANCE);

        if (showProgress) {
            ProgressDialog.showProgress(this, future, SharedLocale.tr("launcher.checkingTitle"), SharedLocale.tr("launcher.checkingStatus"));
        }
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
                        lolnetPingButton.setVisible(true);
                        try {
                            File codeFile = new File(Launcher.dataDir, "codes.txt");
                            if (!codeFile.exists()) {
                                codeFile.createNewFile();
                            }
                            br = new BufferedReader(new FileReader(codeFile));
                            for (String line; (line = br.readLine()) != null;) {
                                if (line.equalsIgnoreCase("launcher:showmethemoney")) {
                                    alreadyAdded = true;
                                }
                            }
                            if (!alreadyAdded) {
                                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(codeFile, true)));
                                out.println("launcher:" + code);
                                out.close();
                                split[i] = "Done";
                            }

                        } catch (Exception e1) {
                            split[i] = "error";
                        }
                    } else if (code.equalsIgnoreCase("IWantToGoPlaces")) {
                        Preferences userNodeForPackage = java.util.prefs.Preferences.userNodeForPackage(Launcher.class);
                        userNodeForPackage.put("IWantToGoPlaces", "true");
                        try {
                            File codeFile = new File(Launcher.dataDir, "codes.txt");
                            if (!codeFile.exists()) {
                                codeFile.createNewFile();
                            }
                            br = new BufferedReader(new FileReader(codeFile));
                            for (String line; (line = br.readLine()) != null;) {
                                if (line.equalsIgnoreCase("launcher:IWantToGoPlaces")) {
                                    alreadyAdded = true;
                                }
                            }
                            if (!alreadyAdded) {
                                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(codeFile, true)));
                                out.println("launcher:" + code);
                                out.close();
                                split[i] = "Done. Restart launcher please.";

                            }

                        } catch (Exception e1) {
                            split[i] = "error";
                        }
                    } else if (code.equalsIgnoreCase("IDontOwnMicrosoft")) {
                        Preferences userNodeForPackage = java.util.prefs.Preferences.userNodeForPackage(Launcher.class);
                        userNodeForPackage.put("IDontOwnMicrosoft", "true");
                        try {
                            File codeFile = new File(Launcher.dataDir, "codes.txt");
                            if (!codeFile.exists()) {
                                codeFile.createNewFile();
                            }
                            br = new BufferedReader(new FileReader(codeFile));
                            for (String line; (line = br.readLine()) != null;) {
                                if (line.equalsIgnoreCase("launcher:IDontOwnMicrosoft")) {
                                    alreadyAdded = true;
                                }
                            }
                            if (!alreadyAdded) {
                                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(codeFile, true)));
                                out.println("launcher:" + code);
                                out.close();
                                split[i] = "Done.";

                            }

                        } catch (Exception e1) {
                            split[i] = "error";
                        }
                    } else if (code.contains("EveryPrivatePack:")) {
                        String[] split1 = code.split(":");
                        if (split1.length == 2) {
                            String code2 = split1[1];

                            try {
                                File codeFile = new File(Launcher.dataDir, "codes.txt");
                                if (!codeFile.exists()) {
                                    codeFile.createNewFile();
                                }
                                br = new BufferedReader(new FileReader(codeFile));
                                for (String line; (line = br.readLine()) != null;) {
                                    if (line.startsWith("EveryPrivatePack:")) {
                                        if (line.split(":")[1].equalsIgnoreCase(code2.replaceAll(" ", ""))) {
                                            alreadyAdded = true;
                                        }
                                    }
                                }

                                if (!alreadyAdded) {
                                    URL oracle = new URL("https://www.lolnet.co.nz/modpack/" + code2 + ".php");
                                    oracle = Launcher.checkURL(oracle);
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
                                    out.println("EveryPrivatePack:" + code2);
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
                        else
                        {
                            split[i] = "WrongCode";
                        }
                    } else {

                        try {
                            File codeFile = new File(Launcher.dataDir, "codes.txt");
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
                                oracle = Launcher.checkURL(oracle);
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
                    loadInstances(true);
                }
                area.setText(text);
            }
        });
    }

    private void launch() {
        try {
            Configuration config = launcher.getConfig();
            if (getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                config.setLauncherWindowHeight(getSize().height);
                config.setLauncherWindowWidth(getSize().width);
            }

            config.setLauncherExtendedState(getExtendedState());
            Persistence.commitAndForget(config);
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
                        this, future, SharedLocale.tr("launcher.updatingTitle"), SharedLocale.tr("launcher.updatingStatus", instance.getTitle()));
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
            SwingHelper.showErrorDialog(this, SharedLocale.tr("launcher.noInstanceError"), SharedLocale.tr("launcher.noInstanceTitle"));
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
                this, processFuture, SharedLocale.tr("launcher.launchingTItle"), SharedLocale.tr("launcher.launchingStatus", instance.getTitle()));

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

    public static JButton getButtonFromImage(final String fileName) {
        ImageIcon imageIcon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/" + fileName + ".png"));
        final JButton jButton = new JButton(imageIcon);
        jButton.setBorder(BorderFactory.createEmptyBorder());
        jButton.setContentAreaFilled(false);

        jButton.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                jButton.setIcon(new ImageIcon(SwingHelper.readIconImage(Launcher.class, "button/" + fileName + "_clicked.png")));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                jButton.setIcon(new ImageIcon(SwingHelper.readIconImage(Launcher.class, "Launcher_button1.png")));
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

        });
        return jButton;
    }

}
