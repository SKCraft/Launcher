/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */
package com.skcraft.launcher.launch;

import com.google.common.base.Function;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.Session;
import com.skcraft.launcher.dialog.LauncherFrame;
import com.skcraft.launcher.dialog.ProcessConsoleFrame;
import com.skcraft.launcher.swing.MessageLog;
import lombok.NonNull;
import lombok.extern.java.Log;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import nz.co.lolnet.statistics.MetaData;
import nz.co.lolnet.statistics.ThreadModPackGameTime;

/**
 * Handles post-process creation during launch.
 */
@Log
public class LaunchProcessHandler implements Function<Process, ProcessConsoleFrame> {

    private static final int CONSOLE_NUM_LINES = 10000;

    private final Launcher launcher;
    private ProcessConsoleFrame consoleFrame;
    private long startTime;
    private long endTime;
    private Instance instance;
    Session session;

    public LaunchProcessHandler(@NonNull Launcher launcher, Instance instance, Session session) {
        this.launcher = launcher;
        this.instance = instance;
        this.session = session;
    }

    @Override
    public ProcessConsoleFrame apply(final Process process) {
        log.info("Watching process " + process);
        startTime = System.currentTimeMillis();

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    consoleFrame = new ProcessConsoleFrame(CONSOLE_NUM_LINES, true);
                    consoleFrame.setProcess(process);
                    consoleFrame.setVisible(true);
                    MessageLog messageLog = consoleFrame.getMessageLog();
                    messageLog.consume(process.getInputStream());
                    messageLog.consume(process.getErrorStream());
                }
            });

            // Wait for the process to end
            process.waitFor();
        } catch (InterruptedException e) {
            // Orphan process
        } catch (InvocationTargetException e) {
            log.log(Level.WARNING, "Unexpected failure", e);
        }

        log.info("Process ended, re-showing launcher...");
        endTime = System.currentTimeMillis();
        final long totalTime = (endTime - startTime) / (1000);

        // Restore the launcher
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LauncherFrame(launcher).setVisible(true);
                if (MetaData.getPermission()) {
                    new ThreadModPackGameTime(session.getName(), instance.getName(), (int) totalTime);
                }
                if (consoleFrame != null) {
                    consoleFrame.setProcess(null);
                    consoleFrame.requestFocus();
                }
            }
        });

        return consoleFrame;
    }

}
