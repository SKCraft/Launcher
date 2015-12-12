/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.bootstrap;

import lombok.NonNull;
import lombok.extern.java.Log;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.skcraft.launcher.bootstrap.BootstrapUtils.closeQuietly;
import static com.skcraft.launcher.bootstrap.SharedLocale.tr;

/**
 * Swing utility methods.
 */
@Log
public final class SwingHelper {

    private SwingHelper() {
    }

    public static String htmlEscape(String str) {
        return str.replace(">", "&gt;")
                .replace("<", "&lt;")
                .replace("&", "&amp;");
    }

    /**
     * Shows an popup error dialog, with potential extra details shown either immediately
     * or available on the dialog.
     *
     * @param parentComponent the frame from which the dialog is displayed, otherwise
     *                        null to use the default frame
     * @param message the message to display
     * @param title the title string for the dialog
     * @see #showMessageDialog(java.awt.Component, String, String, String, int) for details
     */
    public static void showErrorDialog(Component parentComponent, @NonNull String message,
                                       @NonNull String title) {
        showErrorDialog(parentComponent, message, title, null);
    }

    /**
     * Shows an popup error dialog, with potential extra details shown either immediately
     * or available on the dialog.
     *
     * @param parentComponent the frame from which the dialog is displayed, otherwise
     *                        null to use the default frame
     * @param message the message to display
     * @param title the title string for the dialog
     * @param throwable the exception, or null if there is no exception to show
     * @see #showMessageDialog(java.awt.Component, String, String, String, int) for details
     */
    public static void showErrorDialog(Component parentComponent, @NonNull String message,
                                       @NonNull String title, Throwable throwable) {
        String detailsText = null;

        // Get a string version of the exception and use that for
        // the extra details text
        if (throwable != null) {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            detailsText = sw.toString();
        }

        showMessageDialog(parentComponent,
                message, title,
                detailsText, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show a message dialog using
     * {@link javax.swing.JOptionPane#showMessageDialog(java.awt.Component, Object, String, int)}.
     *
     * <p>The dialog will be shown from the Event Dispatch Thread, regardless of the
     * thread it is called from. In either case, the method will block until the
     * user has closed the dialog (or dialog creation fails for whatever reason).</p>
     *
     * @param parentComponent the frame from which the dialog is displayed, otherwise
     *                        null to use the default frame
     * @param message the message to display
     * @param title the title string for the dialog
     * @param messageType see {@link javax.swing.JOptionPane#showMessageDialog(java.awt.Component, Object, String, int)}
     *                    for available message types
     */
    public static void showMessageDialog(final Component parentComponent,
                                         @NonNull final String message,
                                         @NonNull final String title,
                                         final String detailsText,
                                         final int messageType) {

        if (SwingUtilities.isEventDispatchThread()) {
            // To force the label to wrap, convert the message to broken HTML
            String htmlMessage = "<html><div style=\"width: 250px\">" + htmlEscape(message);

            JPanel panel = new JPanel(new BorderLayout(0, detailsText != null ? 20 : 0));

            // Add the main message
            panel.add(new JLabel(htmlMessage), BorderLayout.NORTH);

            // Add the extra details
            if (detailsText != null) {
                JTextArea textArea = new JTextArea(tr("errorDialog.reportError") + "\n\n" + detailsText);
                JLabel tempLabel = new JLabel();
                textArea.setFont(tempLabel.getFont());
                textArea.setBackground(tempLabel.getBackground());
                textArea.setTabSize(2);
                textArea.setEditable(false);;

                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(350, 120));
                panel.add(scrollPane, BorderLayout.CENTER);
            }

            JOptionPane.showMessageDialog(
                    parentComponent, panel, title, messageType);
        } else {
            // Call method again from the Event Dispatch Thread
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        showMessageDialog(
                                parentComponent, message, title,
                                detailsText, messageType);
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Asks the user a binary yes or no question.
     *
     * @param parentComponent the component
     * @param message the message to display
     * @param title the title string for the dialog
     * @return whether 'yes' was selected
     */
    public static boolean confirmDialog(final Component parentComponent,
                                        @NonNull final String message,
                                        @NonNull final String title) {
        if (SwingUtilities.isEventDispatchThread()) {
            return JOptionPane.showConfirmDialog(
                    parentComponent, message, title, JOptionPane.YES_NO_OPTION) ==
                    JOptionPane.YES_OPTION;
        } else {
            // Use an AtomicBoolean to pass the result back from the
            // Event Dispatcher Thread
            final AtomicBoolean yesSelected = new AtomicBoolean();

            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        yesSelected.set(confirmDialog(parentComponent, title, message));
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }

            return yesSelected.get();
        }
    }

    public static BufferedImage readIconImage(Class<?> clazz, String path) {
        InputStream in = null;
        try {
            in = clazz.getResourceAsStream(path);
            if (in != null) {
                return ImageIO.read(in);
            }
        } catch (IOException e) {
        } finally {
            closeQuietly(in);
        }
        return null;
    }

    public static void setIconImage(Window frame, Class<?> clazz, String path) {
        BufferedImage image = readIconImage(clazz, path);
        if (image != null) {
            frame.setIconImage(image);
        }
    }

    /**
     * Focus a component.
     *
     * <p>The focus call happens in {@link javax.swing.SwingUtilities#invokeLater(Runnable)}.</p>
     * 
     * @param component the component
     */
    public static void focusLater(@NonNull final Component component) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (component instanceof JTextComponent) {
                    ((JTextComponent) component).selectAll();
                }
                component.requestFocusInWindow();
            }
        });
    }

}
