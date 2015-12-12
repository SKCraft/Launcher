/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.dialog;

import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.swing.LinedBoxPanel;
import com.skcraft.launcher.swing.MessageLog;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.PastebinPoster;
import com.skcraft.launcher.util.SharedLocale;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static com.skcraft.launcher.util.SharedLocale.tr;

/**
 * A frame capable of showing messages.
 */
public class ConsoleFrame extends JFrame {

    private static ConsoleFrame globalFrame;

    @Getter private final Image trayRunningIcon;
    @Getter private final Image trayClosedIcon;

    @Getter private final MessageLog messageLog;
    @Getter private LinedBoxPanel buttonsPanel;

    private boolean registeredGlobalLog = false;

    /**
     * Construct the frame.
     *
     * @param numLines number of lines to show at a time
     * @param colorEnabled true to enable a colored console
     */
    public ConsoleFrame(int numLines, boolean colorEnabled) {
        this(SharedLocale.tr("console.title"), numLines, colorEnabled);
    }

    /**
     * Construct the frame.
     * 
     * @param title the title of the window
     * @param numLines number of lines to show at a time
     * @param colorEnabled true to enable a colored console
     */
    public ConsoleFrame(@NonNull String title, int numLines, boolean colorEnabled) {
        messageLog = new MessageLog(numLines, colorEnabled);
        trayRunningIcon = SwingHelper.createImage(Launcher.class, "tray_ok.png");
        trayClosedIcon = SwingHelper.createImage(Launcher.class, "tray_closed.png");

        setTitle(title);
        setIconImage(trayRunningIcon);

        setSize(new Dimension(650, 400));
        initComponents();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                performClose();
            }
        });
    }

    /**
     * Add components to the frame.
     */
    private void initComponents() {
        JButton pastebinButton = new JButton(SharedLocale.tr("console.uploadLog"));
        buttonsPanel = new LinedBoxPanel(true);

        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        buttonsPanel.addElement(pastebinButton);

        add(buttonsPanel, BorderLayout.NORTH);
        add(messageLog, BorderLayout.CENTER);

        pastebinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pastebinLog();
            }
        });
    }

    /**
     * Register the global logger if it hasn't been registered.
     */
    private void registerLoggerHandler() {
        if (!registeredGlobalLog) {
            getMessageLog().registerLoggerHandler();
            registeredGlobalLog = true;
        }
    }

    /**
     * Attempt to perform window close.
     */
    protected void performClose() {
        messageLog.detachGlobalHandler();
        messageLog.clear();
        registeredGlobalLog = false;
        dispose();
    }

    /**
     * Send the contents of the message log to a pastebin.
     */
    private void pastebinLog() {
        String text = messageLog.getPastableText();
        // Not really bytes!
        messageLog.log(tr("console.pasteUploading", text.length()), messageLog.asHighlighted());

        PastebinPoster.paste(text, new PastebinPoster.PasteCallback() {
            @Override
            public void handleSuccess(String url) {
                messageLog.log(tr("console.pasteUploaded", url), messageLog.asHighlighted());
                SwingHelper.openURL(url, messageLog);
            }

            @Override
            public void handleError(String err) {
                messageLog.log(tr("console.pasteFailed", err), messageLog.asError());
            }
        });
    }

    public static void showMessages() {
        ConsoleFrame frame = globalFrame;
        if (frame == null) {
            frame = new ConsoleFrame(10000, false);
            globalFrame = frame;
            frame.setTitle(SharedLocale.tr("console.launcherConsoleTitle"));
            frame.registerLoggerHandler();
            frame.setVisible(true);
        } else {
            frame.setVisible(true);
            frame.registerLoggerHandler();
            frame.requestFocus();
        }
    }

    public static void hideMessages() {
        ConsoleFrame frame = globalFrame;
        if (frame != null) {
            frame.setVisible(false);
        }
    }

}
