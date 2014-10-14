/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */
package com.skcraft.launcher.dialog;

import com.skcraft.launcher.Configuration;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.swing.*;
import com.skcraft.launcher.persistence.Persistence;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.skcraft.launcher.util.SharedLocale._;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * A dialog to modify configuration options.
 */
public class ConfigurationDialog extends JDialog {

    private final Configuration config;
    private final ObjectSwingMapper mapper;

    private final JPanel tabContainer = new JPanel(new BorderLayout());
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final FormPanel javaSettingsPanel = new FormPanel();
    private final JTextField jvmPathText = new JTextField();
    private final JTextField jvmArgsText = new JTextField();
    private final JSpinner minMemorySpinner = new JSpinner();
    private final JSpinner maxMemorySpinner = new JSpinner();
    private final JSpinner permGenSpinner = new JSpinner();
    private final FormPanel gameSettingsPanel = new FormPanel();
    private final JSpinner widthSpinner = new JSpinner();
    private final JSpinner heightSpinner = new JSpinner();
    private final FormPanel proxySettingsPanel = new FormPanel();
    private final JCheckBox useProxyCheck = new JCheckBox(_("options.useProxyCheck"));
    private final JTextField proxyHostText = new JTextField();
    private final JSpinner proxyPortText = new JSpinner();
    private final JTextField proxyUsernameText = new JTextField();
    private final JPasswordField proxyPasswordText = new JPasswordField();
    private final FormPanel advancedPanel = new FormPanel();
    private final JTextField gameKeyText = new JTextField();
    private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);
    private final LinedBoxPanel buttonsPanel2 = new LinedBoxPanel(true);
    private final JButton okButton = new JButton(_("button.ok"));
    private final JButton cancelButton = new JButton(_("button.cancel"));
    private final JButton logButton = new JButton(_("options.launcherConsole"));
    private final JButton changeDataStorageLocationButton = new JButton("Change Data Directory...");

    /**
     * Create a new configuration dialog.
     *
     * @param owner the window owner
     * @param launcher the launcher
     */
    public ConfigurationDialog(Window owner, @NonNull Launcher launcher) {
        super(owner, ModalityType.DOCUMENT_MODAL);

        this.config = launcher.getConfig();
        mapper = new ObjectSwingMapper(config);

        setTitle(_("options.title"));
        initComponents();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(400, 500));
        setResizable(false);
        setLocationRelativeTo(owner);

        mapper.map(jvmPathText, "jvmPath");
        mapper.map(jvmArgsText, "jvmArgs");
        mapper.map(minMemorySpinner, "minMemory");
        mapper.map(maxMemorySpinner, "maxMemory");
        mapper.map(permGenSpinner, "permGen");
        mapper.map(widthSpinner, "windowWidth");
        mapper.map(heightSpinner, "widowHeight");
        mapper.map(useProxyCheck, "proxyEnabled");
        mapper.map(proxyHostText, "proxyHost");
        mapper.map(proxyPortText, "proxyPort");
        mapper.map(proxyUsernameText, "proxyUsername");
        mapper.map(proxyPasswordText, "proxyPassword");
        mapper.map(gameKeyText, "gameKey");

        mapper.copyFromObject();
    }

    private void initComponents() {
        javaSettingsPanel.addRow(new JLabel(_("options.jvmPath")), jvmPathText);
        javaSettingsPanel.addRow(new JLabel(_("options.jvmArguments")), jvmArgsText);
        javaSettingsPanel.addRow(Box.createVerticalStrut(15));
        javaSettingsPanel.addRow(new JLabel(_("options.64BitJavaWarning")));
        javaSettingsPanel.addRow(new JLabel(_("options.minMemory")), minMemorySpinner);
        javaSettingsPanel.addRow(new JLabel(_("options.maxMemory")), maxMemorySpinner);
        javaSettingsPanel.addRow(new JLabel(_("options.permGen")), permGenSpinner);
        SwingHelper.removeOpaqueness(javaSettingsPanel);
        tabbedPane.addTab(_("options.javaTab"), SwingHelper.alignTabbedPane(javaSettingsPanel));

        gameSettingsPanel.addRow(new JLabel(_("options.windowWidth")), widthSpinner);
        gameSettingsPanel.addRow(new JLabel(_("options.windowHeight")), heightSpinner);
        SwingHelper.removeOpaqueness(gameSettingsPanel);
        tabbedPane.addTab(_("options.minecraftTab"), SwingHelper.alignTabbedPane(gameSettingsPanel));

        proxySettingsPanel.addRow(useProxyCheck);
        proxySettingsPanel.addRow(new JLabel(_("options.proxyHost")), proxyHostText);
        proxySettingsPanel.addRow(new JLabel(_("options.proxyPort")), proxyPortText);
        proxySettingsPanel.addRow(new JLabel(_("options.proxyUsername")), proxyUsernameText);
        proxySettingsPanel.addRow(new JLabel(_("options.proxyPassword")), proxyPasswordText);
        SwingHelper.removeOpaqueness(proxySettingsPanel);
        tabbedPane.addTab(_("options.proxyTab"), SwingHelper.alignTabbedPane(proxySettingsPanel));

        advancedPanel.addRow(new JLabel(_("options.gameKey")), gameKeyText);
        buttonsPanel2.addGlue();
        buttonsPanel2.addElement(changeDataStorageLocationButton);
        advancedPanel.add(buttonsPanel2);
        SwingHelper.removeOpaqueness(advancedPanel);
        tabbedPane.addTab(_("options.advancedTab"), SwingHelper.alignTabbedPane(advancedPanel));

        buttonsPanel.addElement(logButton);
        buttonsPanel.addGlue();
        buttonsPanel.addElement(okButton);
        buttonsPanel.addElement(cancelButton);

        tabContainer.add(tabbedPane, BorderLayout.CENTER);
        tabContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(tabContainer, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        SwingHelper.equalWidth(okButton, cancelButton);

        cancelButton.addActionListener(ActionListeners.dispose(this));

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });

        logButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConsoleFrame.showMessages();
            }
        });

        changeDataStorageLocationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Preferences userNodeForPackage = java.util.prefs.Preferences.userNodeForPackage(Launcher.class);
                String currentPath = userNodeForPackage.get("LolnetLauncherDataPath", "");
                if (currentPath == null || currentPath.equalsIgnoreCase("")) {
                    currentPath = Launcher.dataDir.getAbsolutePath();
                }
                String FilePath = JOptionPane.showInputDialog("Enter Data Storage Location",
                        currentPath);
                if (FilePath == null || FilePath.equalsIgnoreCase("")) {
                    return;
                }
                File file = new File(new File(FilePath).getParent());

                if (file.exists()) {
                    File folder = new File(FilePath);
                    boolean mkdirs = folder.mkdirs();
                    if (folder.exists() || folder.mkdirs()) {
                        String oldPath = userNodeForPackage.get("LolnetLauncherDataPath", "");
                        userNodeForPackage.put("LolnetLauncherDataPath", FilePath);
                        if (oldPath == null || oldPath.equalsIgnoreCase("")) {
                            JOptionPane.showMessageDialog(null, "Changed. New path is now: " + FilePath, "success" + "\n Please restart Launcher to take effect", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Path Changed from " + oldPath + ".\n New path is now: " + FilePath + "\n Please restart Launcher to take effect", "success", JOptionPane.INFORMATION_MESSAGE);
                        }
                        if (Launcher.launcherJarFile.getName().contains(".jar")) {
                            if (JOptionPane.showConfirmDialog(null, "Would you like to restart now?", "Restart?",
                                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                                try {
                                    Runtime rt = Runtime.getRuntime();
                                    String path = Launcher.launcherJarFile.getAbsolutePath().replaceAll("%20", " ");
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

                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to create directory. Do you have permission?", "Error: no permission", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "File Path does not exist: " + file.getPath(), "Error: No Path Found", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    /**
     * Save the configuration and close the dialog.
     */
    public void save() {
        mapper.copyFromSwing();
        Persistence.commitAndForget(config);
        dispose();
    }
}
