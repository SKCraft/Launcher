/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.dialog;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.InstanceList;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.Session;
import com.skcraft.launcher.launch.Runner;
import com.skcraft.launcher.launch.LaunchProcessHandler;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.selfupdate.UpdateChecker;
import com.skcraft.launcher.selfupdate.SelfUpdater;
import com.skcraft.launcher.swing.*;
import com.skcraft.launcher.update.HardResetter;
import com.skcraft.launcher.update.Remover;
import com.skcraft.launcher.update.Updater;
import com.skcraft.launcher.util.SwingExecutor;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;

import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;
import static com.skcraft.launcher.util.SharedLocale._;

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
        setSize(700, 450);
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
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(4);
        SwingHelper.flattenJSplitPane(splitPane);
        buttonsPanel.addElement(refreshButton);
        buttonsPanel.addElement(updateCheck);
        buttonsPanel.addGlue();
        buttonsPanel.addElement(selfUpdateButton);
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
        selfUpdateButton.setVisible(true);
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
        ProgressDialog.showProgress( this, future, _("instance.resettingTitle"),
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
