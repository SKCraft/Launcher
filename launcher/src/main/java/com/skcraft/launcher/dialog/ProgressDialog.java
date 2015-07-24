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
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.swing.LinedBoxPanel;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;
import lombok.extern.java.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import static com.skcraft.launcher.util.SharedLocale.tr;

@Log
public class ProgressDialog extends JDialog {

    private static WeakReference<ProgressDialog> lastDialogRef;

    private final String defaultTitle;
    private final String defaultMessage;
    private final JLabel label = new JLabel();
    private final JPanel progressPanel = new JPanel(new BorderLayout(0, 5));
    private final JPanel textAreaPanel = new JPanel(new BorderLayout());
    private final JProgressBar progressBar = new JProgressBar();
    private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);
    private final JTextArea logText = new JTextArea();
    private final JScrollPane logScroll = new JScrollPane(logText);
    private final JButton detailsButton = new JButton();
    private final JButton logButton = new JButton(SharedLocale.tr("progress.viewLog"));
    private final JButton cancelButton = new JButton(SharedLocale.tr("button.cancel"));

    public ProgressDialog(Window owner, String title, String message) {
        super(owner, title, ModalityType.DOCUMENT_MODAL);

        setResizable(false);
        initComponents();
        label.setText(message);
        defaultTitle = title;
        defaultMessage = message;
        setCompactSize();
        setLocationRelativeTo(owner);

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

    private void setCompactSize() {
        detailsButton.setText(SharedLocale.tr("progress.details"));
        logButton.setVisible(false);
        setMinimumSize(new Dimension(400, 100));
        pack();
    }

    private void setDetailsSize() {
        detailsButton.setText(SharedLocale.tr("progress.less"));
        logButton.setVisible(true);
        setSize(400, 350);
    }

    private void initComponents() {
        progressBar.setMaximum(1000);
        progressBar.setMinimum(0);
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(0, 18));

        buttonsPanel.addElement(detailsButton);
        buttonsPanel.addElement(logButton);
        buttonsPanel.addGlue();
        buttonsPanel.addElement(cancelButton);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(30, 13, 13, 13));

        logScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        logText.setBackground(getBackground());
        logText.setEditable(false);
        logText.setLineWrap(true);
        logText.setWrapStyleWord(false);
        logText.setFont(new JLabel().getFont());

        progressPanel.add(label, BorderLayout.NORTH);
        progressPanel.setBorder(BorderFactory.createEmptyBorder(13, 13, 0, 13));
        progressPanel.add(progressBar, BorderLayout.CENTER);
        textAreaPanel.setBorder(BorderFactory.createEmptyBorder(10, 13, 0, 13));
        textAreaPanel.add(logScroll, BorderLayout.CENTER);

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

        detailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleDetails();
            }
        });

        logButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConsoleFrame.showMessages();
            }
        });
    }

    private boolean confirmCancel() {
        return SwingHelper.confirmDialog(this, SharedLocale.tr("progress.confirmCancel"), SharedLocale.tr("progress.confirmCancelTitle"));
    }

    protected void cancel() {
    }

    private void toggleDetails() {
        if (textAreaPanel.isVisible()) {
            textAreaPanel.setVisible(false);
            setCompactSize();
        } else {
            textAreaPanel.setVisible(true);
            setDetailsSize();
        }
        setLocationRelativeTo(getOwner());
    }

    public static void showProgress(final Window owner, final ObservableFuture<?> future, String title, String message) {
        showProgress(owner, future, future, title, message);
    }

    public static void showProgress(final Window owner, final ListenableFuture<?> future, ProgressObservable observable, String title, String message) {
        final ProgressDialog dialog = new ProgressDialog(owner, title, message) {
            @Override
            protected void cancel() {
                future.cancel(true);
            }
        };

        lastDialogRef = new WeakReference<ProgressDialog>(dialog);

        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new UpdateProgress(dialog, observable), 400, 400);

        Futures.addCallback(future, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                timer.cancel();
                dialog.dispose();
            }

            @Override
            public void onFailure(Throwable t) {
                timer.cancel();
                dialog.dispose();
            }
        }, SwingExecutor.INSTANCE);

        dialog.setVisible(true);
    }

    public static ProgressDialog getLastDialog() {
        WeakReference<ProgressDialog> ref = lastDialogRef;
        if (ref != null) {
            return ref.get();
        }

        return null;
    }

    private static class UpdateProgress extends TimerTask {
        private final ProgressDialog dialog;
        private final ProgressObservable observable;

        public UpdateProgress(ProgressDialog dialog, ProgressObservable observable) {
            this.dialog = dialog;
            this.observable = observable;
        }

        @Override
        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JProgressBar progressBar = dialog.progressBar;
                    JTextArea logText = dialog.logText;
                    JLabel label = dialog.label;

                    double progress = observable.getProgress();
                    if (progress >= 0) {
                        dialog.setTitle(tr("progress.percentTitle",
                                Math.round(progress * 100 * 100) / 100.0, dialog.defaultTitle));
                        progressBar.setValue((int) (progress * 1000));
                        progressBar.setIndeterminate(false);
                    } else {
                        dialog.setTitle( dialog.defaultTitle);
                        progressBar.setIndeterminate(true);
                    }

                    String status = observable.getStatus();
                    if (status == null) {
                        status = SharedLocale.tr("progress.defaultStatus");
                        label.setText(dialog.defaultMessage);
                    } else {
                        int index = status.indexOf('\n');
                        if (index == -1) {
                            label.setText(status);
                        } else {
                            label.setText(status.substring(0, index));
                        }
                    }
                    logText.setText(status);
                    logText.setCaretPosition(0);
                }
            });
        }
    }

}
