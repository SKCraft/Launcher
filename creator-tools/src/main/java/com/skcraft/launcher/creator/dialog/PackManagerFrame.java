/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.dialog;

import com.jidesoft.swing.SearchableUtils;
import com.jidesoft.swing.TableSearchable;
import com.skcraft.launcher.creator.Creator;
import com.skcraft.launcher.swing.DefaultTable;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.swing.TableColumnAdjuster;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class PackManagerFrame extends JFrame {

    @Getter private final JButton newPackButton = new JButton("New Pack", SwingHelper.createIcon(Creator.class, "new.png"));
    @Getter private final JButton importButton = new JButton("Add Existing", SwingHelper.createIcon(Creator.class, "import.png"));
    @Getter private final JButton editConfigButton = new JButton("Modify", SwingHelper.createIcon(Creator.class, "edit.png"));
    @Getter private final JButton openFolderButton = new JButton("Open", SwingHelper.createIcon(Creator.class, "open_folder.png"));
    @Getter private final JButton checkProblemsButton = new JButton("Check", SwingHelper.createIcon(Creator.class, "check.png"));
    @Getter private final JButton testButton = new JButton("Test", SwingHelper.createIcon(Creator.class, "test.png"));
    @Getter private final JButton buildButton = new JButton("Build", SwingHelper.createIcon(Creator.class, "build.png"));

    @Getter private final JMenuItem newPackMenuItem = new JMenuItem("New Pack...");
    @Getter private final JMenuItem newPackAtLocationMenuItem = new JMenuItem("New Pack at Location...");
    @Getter private final JMenuItem importPackMenuItem = new JMenuItem("Add Existing Pack...");
    @Getter private final JMenuItem changePackLocationMenuItem = new JMenuItem("Change Pack Location...");
    @Getter private final JMenuItem refreshMenuItem = new JMenuItem("Reload Workspace");
    @Getter private final JMenuItem removePackItem = new JMenuItem("Remove Pack...");
    @Getter private final JMenuItem deletePackItem = new JMenuItem("Delete Pack Forever...");
    @Getter private final JMenuItem quitMenuItem = new JMenuItem("Exit");
    @Getter private final JMenuItem editConfigMenuItem = new JMenuItem("Edit modpack.json...");
    @Getter private final JMenuItem openFolderMenuItem = new JMenuItem("Open Directory");
    @Getter private final JMenuItem checkProblemsMenuItem = new JMenuItem("Scan for Problems...");
    @Getter private final JMenuItem testMenuItem = new JMenuItem("Test");
    @Getter private final JMenuItem testOnlineMenuItem = new JMenuItem("Test Online");
    @Getter private final JMenuItem optionsMenuItem = new JMenuItem("Test Launcher Options...");
    @Getter private final JMenuItem clearInstanceMenuItem = new JMenuItem("Delete Test Launcher Instances");
    @Getter private final JMenuItem clearWebRootMenuItem = new JMenuItem("Empty Test Web Server");
    @Getter private final JMenuItem buildMenuItem = new JMenuItem("Build Pack...");
    @Getter private final JMenuItem deployServerMenuItem = new JMenuItem("Deploy Server...");
    @Getter private final JMenuItem generatePackagesMenuItem = new JMenuItem("Generate packages.json...");
    @Getter private final JMenuItem openOutputFolderMenuItem = new JMenuItem("Open Upload Folder");
    @Getter private final JMenuItem versionCheckMenuItem = new JMenuItem("Check for Mod Updates");
    @Getter private final JMenuItem openWorkspaceFolderMenuItem = new JMenuItem("Open Workspace Folder");
    @Getter private final JMenuItem openLauncherFolderMenuItem = new JMenuItem("Open Test Launcher Folder");
    @Getter private final JMenuItem openWebRootMenuItem = new JMenuItem("Open Test Web Server Folder");
    @Getter private final JMenuItem openConsoleMenuItem = new JMenuItem("Open Console");
    @Getter private final JMenuItem docsMenuItem = new JMenuItem("Documentation");
    @Getter private final JMenuItem aboutMenuItem = new JMenuItem("About");

    @Getter private final JTable packTable = new DefaultTable();

    public PackManagerFrame() {
        super("Modpack Creator");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        initComponents();
        initMenu();
        pack();
        setLocationRelativeTo(null);

        SwingHelper.setFrameIcon(this, Creator.class, "icon.png");
    }

    private void initComponents() {
        TableColumnAdjuster adjuster = new TableColumnAdjuster(packTable);
        adjuster.adjustColumns();
        adjuster.setDynamicAdjustment(true);

        packTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        packTable.setAutoCreateRowSorter(true);

        JPanel container = new JPanel();
        container.setLayout(new MigLayout("fill, wrap 1"));

        container.add(createToolbar(), "dock north");
        container.add(SwingHelper.wrapScrollPane(packTable), "grow, span, w null:800:null");

        TableSearchable tableSearchable = SearchableUtils.installSearchable(packTable);
        tableSearchable.setMainIndex(-1);

        add(container, BorderLayout.CENTER);
    }

    private JToolBar createToolbar() {
        JToolBar toolBar = new JToolBar("Toolbar");

        toolBar.setFloatable(false);

        toolBar.add(newPackButton);
        toolBar.add(importButton);
        toolBar.addSeparator();
        toolBar.add(editConfigButton);
        toolBar.add(openFolderButton);
        toolBar.add(checkProblemsButton);
        toolBar.addSeparator();
        toolBar.add(testButton);
        toolBar.add(buildButton);

        return toolBar;
    }

    private void initMenu() {
        int ctrlKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        newPackMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ctrlKeyMask));
        newPackAtLocationMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ctrlKeyMask | Event.SHIFT_MASK));
        editConfigMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ctrlKeyMask));
        openFolderMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ctrlKeyMask | Event.SHIFT_MASK));
        testMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        testOnlineMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        buildMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, Event.SHIFT_MASK));
        deployServerMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, Event.SHIFT_MASK));
        docsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

        JMenuBar menuBar;
        JMenu menu;

        menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createEmptyBorder());

        Insets menuInset = new Insets(2, 2, 2, 2);

        menu = new JMenu("File");
        menu.setMargin(menuInset);
        menu.setMnemonic('f');
        menuBar.add(menu);
        menu.add(newPackMenuItem);
        menu.add(newPackAtLocationMenuItem);
        menu.add(importPackMenuItem);
        menu.addSeparator();
        menu.add(changePackLocationMenuItem);
        menu.add(removePackItem);
        menu.add(deletePackItem);
        menu.addSeparator();
        menu.add(refreshMenuItem);
        menu.addSeparator();
        menu.add(quitMenuItem);

        menu = new JMenu("Edit");
        menu.setMargin(menuInset);
        menu.setMnemonic('e');
        menuBar.add(menu);
        menu.add(editConfigMenuItem);
        menu.add(openFolderMenuItem);
        menu.addSeparator();
        menu.add(checkProblemsMenuItem);

        menu = new JMenu("Test");
        menu.setMargin(menuInset);
        menu.setMnemonic('t');
        menuBar.add(menu);
        menu.add(testMenuItem);
        menu.add(testOnlineMenuItem);
        menu.addSeparator();
        menu.add(optionsMenuItem);
        menu.addSeparator();
        menu.add(clearInstanceMenuItem);
        menu.add(clearWebRootMenuItem);

        menu = new JMenu("Build");
        menu.setMargin(menuInset);
        menu.setMnemonic('b');
        menuBar.add(menu);
        menu.add(buildMenuItem);
        menu.add(deployServerMenuItem);
        menu.addSeparator();
        menu.add(generatePackagesMenuItem);
        menu.addSeparator();
        menu.add(openOutputFolderMenuItem);

        menu = new JMenu("Tools");
        menu.setMargin(menuInset);
        menu.setMnemonic('t');
        menuBar.add(menu);
        menu.add(versionCheckMenuItem);
        menu.addSeparator();
        menu.add(openWorkspaceFolderMenuItem);
        menu.add(openLauncherFolderMenuItem);
        menu.add(openWebRootMenuItem);
        menu.addSeparator();
        menu.add(openConsoleMenuItem);

        menu = new JMenu("Help");
        menu.setMargin(menuInset);
        menu.setMnemonic('h');
        menuBar.add(menu);
        menu.add(docsMenuItem);
        menu.addSeparator();
        menu.add(aboutMenuItem);

        setJMenuBar(menuBar);
    }

}
