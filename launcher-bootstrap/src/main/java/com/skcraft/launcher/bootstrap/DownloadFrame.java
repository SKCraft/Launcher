/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.bootstrap;

import com.skcraft.launcher.Bootstrap;
import lombok.extern.java.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;

import static com.skcraft.launcher.bootstrap.SharedLocale.tr;

@Log
public class DownloadFrame extends JFrame {

    private Downloader downloader;
    private Timer timer;

    private final JLabel label = new JLabel();
    private final JPanel progressPanel = new JPanel(new BorderLayout(0, 5));
    private final JPanel textAreaPanel = new JPanel(new BorderLayout());
    private final JProgressBar progressBar = new JProgressBar();
    private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);
    private final JButton cancelButton = new JButton(tr("button.cancal"));

    public DownloadFrame(ProgressObservable observable) {
        super(tr("downloader.title"));
        setResizable(false);
        initComponents();
        label.setText(tr("downloader.pleaseWait"));
        setMinimumSize(new Dimension(400, 100));
        pack();
        setLocationRelativeTo(null);
        SwingHelper.setIconImage(this, Bootstrap.class, "bootstrapper_icon.png");

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                if (confirmCancel()) {
                    cancel();
                    dispose();
                }
            }
        });
    }

    private void initComponents() {
        buttonsPanel.addGlue();
        buttonsPanel.addElement(cancelButton);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(30, 13, 13, 13));;

        progressBar.setIndeterminate(true);
        progressBar.setMinimum(0);
        progressBar.setMaximum(1000);
        progressBar.setPreferredSize(new Dimension(0, 16));

        progressPanel.add(label, BorderLayout.NORTH);
        progressPanel.setBorder(BorderFactory.createEmptyBorder(13, 13, 0, 13));
        progressPanel.add(progressBar, BorderLayout.CENTER);
        textAreaPanel.setBorder(BorderFactory.createEmptyBorder(10, 13, 0, 13));

        add(progressPanel, BorderLayout.NORTH);
        add(textAreaPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        textAreaPanel.setVisible(false);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (confirmCancel()) {
                    cancel();
                    dispose();
                }
            }
        });
    }

    private boolean confirmCancel() {
        return SwingHelper.confirmDialog(this, tr("progress.confirmCancel"), tr("progress.confirmCancelTitle"));
    }

    private void cancel() {
        Downloader downloader = this.downloader;
        if (downloader != null) {
            downloader.cancel();
        } else {
            System.exit(0);
        }
    }

    public synchronized void setDownloader(Downloader downloader) {
        this.downloader = downloader;

        if (downloader == null) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        } else {
            if (timer == null) {
                timer = new Timer();
                timer.scheduleAtFixedRate(new UpdateProgress( downloader), 500, 500);
            }
        }
    }

    private class UpdateProgress extends TimerTask {
        private final Downloader downloader;

        public UpdateProgress(Downloader downloader) {
            this.downloader = downloader;
        }

        @Override
        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Downloader downloader = DownloadFrame.this.downloader;
                    if (downloader != null) {
                        label.setText(downloader.getStatus());
                        double progress = downloader.getProgress();
                        if (progress < 0) {
                            progressBar.setIndeterminate(true);
                        } else {
                            progressBar.setIndeterminate(false);
                            progressBar.setValue((int) (1000 * progress));
                        }
                    } else {
                        label.setText(tr("downloader.pleaseWait"));
                        progressBar.setIndeterminate(true);
                    }
                }
            });
        }
    }

}
