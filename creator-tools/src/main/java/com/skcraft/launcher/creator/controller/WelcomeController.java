/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.controller;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.skcraft.concurrency.Deferred;
import com.skcraft.concurrency.Deferreds;
import com.skcraft.concurrency.SettableProgress;
import com.skcraft.launcher.creator.Creator;
import com.skcraft.launcher.creator.dialog.AboutDialog;
import com.skcraft.launcher.creator.dialog.PackManagerFrame;
import com.skcraft.launcher.creator.dialog.WelcomeDialog;
import com.skcraft.launcher.creator.model.creator.RecentEntry;
import com.skcraft.launcher.creator.model.creator.Workspace;
import com.skcraft.launcher.creator.model.swing.RecentListModel;
import com.skcraft.launcher.creator.swing.WorkspaceDirectoryFilter;
import com.skcraft.launcher.dialog.ProgressDialog;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.PopupMouseAdapter;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.MorePaths;
import com.skcraft.launcher.util.SwingExecutor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class WelcomeController {

    private final WelcomeDialog dialog;
    private final Creator creator;
    private final RecentListModel recentListModel;

    public WelcomeController(WelcomeDialog dialog, Creator creator) {
        this.dialog = dialog;
        this.creator = creator;

        initListeners();

        recentListModel = new RecentListModel(creator.getConfig().getRecentEntries());
        dialog.getRecentList().setModel(recentListModel);
    }

    public void show() {
        dialog.setVisible(true);
    }

    private boolean openWorkspace(File dir) {
        ListeningExecutorService executor = creator.getExecutor();
        PackManagerFrame frame = new PackManagerFrame();
        Deferred<?> deferred = Deferreds.makeDeferred(executor.submit(() -> {
            PackManagerController controller = new PackManagerController(frame, dir, creator);
            addRecentEntry(dir);
            return controller;
        }), executor)
                .handleAsync(PackManagerController::show, ex -> {}, SwingExecutor.INSTANCE);
        ProgressDialog.showProgress(frame, deferred, new SettableProgress("Loading...", -1), "Loading workspace...", "Loading workspace...");
        SwingHelper.addErrorDialogCallback(frame, deferred);

        return true;
    }

    private void addRecentEntry(File dir) {
        List<RecentEntry> newEntries = creator.getConfig().getRecentEntries()
                .stream()
                .filter(entry -> {
                    try {
                        return !MorePaths.isSamePath(entry.getPath(), dir);
                    } catch (IOException ignored) {
                        return false;
                    }
                })
                .collect(Collectors.toCollection(Lists::newArrayList));

        RecentEntry recent = new RecentEntry();
        recent.setPath(dir);
        newEntries.add(0, recent);

        creator.getConfig().setRecentEntries(newEntries);

        Persistence.commitAndForget(creator.getConfig());

        recentListModel.fireUpdate();
    }

    private void removeRecentEntry(RecentEntry entry) {
        creator.getConfig().getRecentEntries().remove(entry);
        Persistence.commitAndForget(creator.getConfig());

        recentListModel.fireUpdate();
    }

    private Optional<RecentEntry> getSelectedRecentEntry() {
        int selectedIndex = dialog.getRecentList().getSelectedIndex();
        if (selectedIndex >= 0) {
            return Optional.fromNullable(creator.getConfig().getRecentEntries().get(selectedIndex));
        } else {
            return Optional.absent();
        }
    }

    private Optional<File> getWorkspaceDir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Workspace Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileFilter(new WorkspaceDirectoryFilter());

        int returnVal = chooser.showOpenDialog(dialog);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return Optional.fromNullable(chooser.getSelectedFile());
        } else {
            return Optional.absent();
        }
    }

    private void initListeners() {
        dialog.getRecentList().addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JList<RecentEntry> table = (JList<RecentEntry>) e.getSource();
                    Point point = e.getPoint();
                    int selectedIndex = table.locationToIndex(point);
                    if (selectedIndex >= 0) {
                        table.setSelectedIndex(selectedIndex);
                        Optional<RecentEntry> optional = getSelectedRecentEntry();
                        if (optional.isPresent()) {
                            if (openWorkspace(optional.get().getPath())) {
                                dialog.dispose();
                            }
                        }
                    }
                }
            }
        });

        dialog.getRecentList().addMouseListener(new PopupMouseAdapter() {
            @Override
            protected void showPopup(MouseEvent e) {
                JList<RecentEntry> table = (JList<RecentEntry>) e.getSource();
                Point point = e.getPoint();
                int selectedIndex = table.locationToIndex(point);
                if (selectedIndex >= 0) {
                    table.setSelectedIndex(selectedIndex);
                    Optional<RecentEntry> optional = getSelectedRecentEntry();
                    if (optional.isPresent()) {
                        popupRecentWorkspaceMenu(e.getComponent(), e.getX(), e.getY(), optional.get());
                    }
                }
            }
        });

        dialog.getNewButton().addActionListener(e -> {
            Optional<File> optional = getWorkspaceDir();
            if (optional.isPresent()) {
                File workspaceFile = Workspace.getWorkspaceFile(optional.get());
                if (!workspaceFile.exists() || SwingHelper.confirmDialog(dialog, "There is already a workspace there. Do you want to load it?", "Existing")) {
                    if (openWorkspace(optional.get())) {
                        dialog.dispose();
                    }
                }
            }
        });

        dialog.getOpenButton().addActionListener(e -> {
            Optional<File> optional = getWorkspaceDir();
            if (optional.isPresent()) {
                File workspaceFile = Workspace.getWorkspaceFile(optional.get());
                if (workspaceFile.exists() || SwingHelper.confirmDialog(dialog, "Do you want to create a new workspace there?", "Create New")) {
                    if (openWorkspace(optional.get())) {
                        dialog.dispose();
                    }
                }
            }
        });

        dialog.getHelpButton().addActionListener(e -> {
            SwingHelper.openURL("https://github.com/SKCraft/Launcher/wiki", dialog);
        });

        dialog.getAboutButton().addActionListener(e -> {
            AboutDialog.showAboutDialog(dialog);
        });

        dialog.getQuitButton().addActionListener(e -> {
            dialog.dispose();
        });
    }

    private void popupRecentWorkspaceMenu(Component component, int x, int y, RecentEntry entry) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem;

        menuItem = new JMenuItem("Remove");
        menuItem.addActionListener(e -> removeRecentEntry(entry));
        popup.add(menuItem);

        popup.show(component, x, y);
    }

}
