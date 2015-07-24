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
import com.skcraft.launcher.util.SharedLocale;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    private final JCheckBox useProxyCheck = new JCheckBox(SharedLocale.tr("options.useProxyCheck"));
    private final JTextField proxyHostText = new JTextField();
    private final JSpinner proxyPortText = new JSpinner();
    private final JTextField proxyUsernameText = new JTextField();
    private final JPasswordField proxyPasswordText = new JPasswordField();
    private final FormPanel advancedPanel = new FormPanel();
    private final JTextField gameKeyText = new JTextField();
    private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);
    private final JButton okButton = new JButton(SharedLocale.tr("button.ok"));
    private final JButton cancelButton = new JButton(SharedLocale.tr("button.cancel"));
    private final JButton aboutButton = new JButton(SharedLocale.tr("options.about"));
    private final JButton logButton = new JButton(SharedLocale.tr("options.launcherConsole"));

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

        setTitle(SharedLocale.tr("options.title"));
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
        javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.jvmPath")), jvmPathText);
        javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.jvmArguments")), jvmArgsText);
        javaSettingsPanel.addRow(Box.createVerticalStrut(15));
        javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.64BitJavaWarning")));
        javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.minMemory")), minMemorySpinner);
        javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.maxMemory")), maxMemorySpinner);
        javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.permGen")), permGenSpinner);
        SwingHelper.removeOpaqueness(javaSettingsPanel);
        tabbedPane.addTab(SharedLocale.tr("options.javaTab"), SwingHelper.alignTabbedPane(javaSettingsPanel));

        gameSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.windowWidth")), widthSpinner);
        gameSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.windowHeight")), heightSpinner);
        SwingHelper.removeOpaqueness(gameSettingsPanel);
        tabbedPane.addTab(SharedLocale.tr("options.minecraftTab"), SwingHelper.alignTabbedPane(gameSettingsPanel));

        proxySettingsPanel.addRow(useProxyCheck);
        proxySettingsPanel.addRow(new JLabel(SharedLocale.tr("options.proxyHost")), proxyHostText);
        proxySettingsPanel.addRow(new JLabel(SharedLocale.tr("options.proxyPort")), proxyPortText);
        proxySettingsPanel.addRow(new JLabel(SharedLocale.tr("options.proxyUsername")), proxyUsernameText);
        proxySettingsPanel.addRow(new JLabel(SharedLocale.tr("options.proxyPassword")), proxyPasswordText);
        SwingHelper.removeOpaqueness(proxySettingsPanel);
        tabbedPane.addTab(SharedLocale.tr("options.proxyTab"), SwingHelper.alignTabbedPane(proxySettingsPanel));

        advancedPanel.addRow(new JLabel(SharedLocale.tr("options.gameKey")), gameKeyText);
        SwingHelper.removeOpaqueness(advancedPanel);
        tabbedPane.addTab(SharedLocale.tr("options.advancedTab"), SwingHelper.alignTabbedPane(advancedPanel));

        buttonsPanel.addElement(logButton);
        buttonsPanel.addElement(aboutButton);
        buttonsPanel.addGlue();
        buttonsPanel.addElement(okButton);
        buttonsPanel.addElement(cancelButton);

        tabContainer.add(tabbedPane, BorderLayout.CENTER);
        tabContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(tabContainer, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        SwingHelper.equalWidth(okButton, cancelButton);

        cancelButton.addActionListener(ActionListeners.dispose(this));

        aboutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AboutDialog.showAboutDialog(ConfigurationDialog.this);
            }
        });

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
