/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.launcher.LauncherException;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;
import lombok.NonNull;
import lombok.extern.java.Log;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import static com.skcraft.launcher.util.SharedLocale.tr;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Swing utility methods.
 */
@Log
public final class SwingHelper {

    private static final ClipboardOwner clipboardOwner = new ClipboardOwner() {
        @Override
        public void lostOwnership(Clipboard clipboard, Transferable contents) {

        }
    };

    private static final Joiner NEW_LINE_JOINER = Joiner.on("\n");

    private SwingHelper() {
    }

    public static String htmlEscape(String str) {
        return str.replace(">", "&gt;")
                .replace("<", "&lt;")
                .replace("&", "&amp;");
    }

    public static void setClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(text), clipboardOwner);
    }

    public static void browseDir(File file, Component component) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(component, tr("errors.openDirError", file.getAbsolutePath()),
                    SharedLocale.tr("errorTitle"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Opens a system web browser for the given URL.
     *
     * @param url the URL
     * @param parentComponent the component from which to show any errors
     */
    public static void openURL(@NonNull String url, @NonNull Component parentComponent) {
        try {
            openURL(new URL(url), parentComponent);
        } catch (MalformedURLException e) {
        }
    }

    /**
     * Opens a system web browser for the given URL.
     *
     * @param url the URL
     * @param parentComponent the component from which to show any errors
     */
    public static void openURL(URL url, Component parentComponent) {
        try {
            Desktop.getDesktop().browse(url.toURI());
        } catch (IOException e) {
            showErrorDialog(parentComponent, tr("errors.openUrlError", url.toString()), SharedLocale.tr("errorTitle"));
        } catch (URISyntaxException e) {
        }
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
                JTextArea textArea = new JTextArea(SharedLocale.tr("errors.reportErrorPreface") + detailsText);
                JLabel tempLabel = new JLabel();
                textArea.setFont(tempLabel.getFont());
                textArea.setBackground(tempLabel.getBackground());
                textArea.setTabSize(2);
                textArea.setEditable(false);
                textArea.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);

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

    /**
     * Equalize the width of the given components.
     *
     * @param component component
     */
    public static void equalWidth(Component ... component) {
        double widest = 0;
        for (Component comp : component) {
            Dimension dim = comp.getPreferredSize();
            if (dim.getWidth() > widest) {
                widest = dim.getWidth();
            }
        }

        for (Component comp : component) {
            Dimension dim = comp.getPreferredSize();
            comp.setPreferredSize(new Dimension((int) widest, (int) dim.getHeight()));
        }
    }

    /**
     * Remove all the opaqueness of the given components and child components.
     *
     * @param components list of components
     */
    public static void removeOpaqueness(@NonNull Component ... components) {
        for (Component component : components) {
            if (component instanceof JComponent) {
                JComponent jComponent = (JComponent) component;
                jComponent.setOpaque(false);
                removeOpaqueness(jComponent.getComponents());
            }
        }
    }

    public static Image createImage(Class<?> clazz, String name) {
        URL resource = clazz.getResource(name);
        if (resource != null) {
            return Toolkit.getDefaultToolkit().createImage(resource);
        } else {
            log.log(Level.WARNING, "Failed to read image from resource: " + name);
            return null;
        }
    }

    public static Icon createIcon(Class<?> clazz, String name) {
        Image image = createImage(clazz, name);
        if (image != null) {
            return new ImageIcon(image);
        } else {
            return new EmptyIcon(16, 16);
        }
    }

    public static Icon createIcon(Class<?> clazz, String name, int width, int height) {
        Image image = createImage(clazz, name);
        if (image != null) {
            return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        } else {
            return new EmptyIcon(width, height);
        }
    }

    public static BufferedImage readBufferedImage(Class<?> clazz, String path) {
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

    public static void setFrameIcon(JFrame frame, Class<?> clazz, String path) {
        BufferedImage image = readBufferedImage(clazz, path);
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

    public static void flattenJSplitPane(JSplitPane splitPane) {
        splitPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        BasicSplitPaneUI flatDividerSplitPaneUI = new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    @Override
                    public void setBorder(Border b) {
                    }
                };
            }
        };
        splitPane.setUI(flatDividerSplitPaneUI);
        splitPane.setBorder(null);
    }

    public static void addErrorDialogCallback(final Window owner, ListenableFuture<?> future) {
        Futures.addCallback(future, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
            }

            @Override
            public void onFailure(Throwable t) {
                if (t instanceof InterruptedException || t instanceof CancellationException) {
                    return;
                }

                String message;
                if (t instanceof LauncherException) {
                    message = t.getLocalizedMessage();
                    t = t.getCause();
                } else {
                    message = t.getLocalizedMessage();
                    if (message == null) {
                        message = SharedLocale.tr("errors.genericError");
                    }
                }
                log.log(Level.WARNING, "Task failed", t);
                SwingHelper.showErrorDialog(owner, message, SharedLocale.tr("errorTitle"), t);
            }
        }, SwingExecutor.INSTANCE);
    }

    public static Component alignTabbedPane(Component component) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(component);
        container.add(new Box.Filler(new Dimension(0, 0), new Dimension(0, 10000), new Dimension(0, 10000)));
        SwingHelper.removeOpaqueness(container);
        return container;
    }

    public static void setTextAndResetCaret(JTextComponent component, String text) {
        component.setText(text);
        component.setCaretPosition(0);
    }

    public static JScrollPane wrapScrollPane(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        return scrollPane;
    }

    public static String listToLines(List<String> lines) {
        return SwingHelper.NEW_LINE_JOINER.join(lines);
    }

    public static List<String> linesToList(String text) {
        String[] tokens = text.replace("\r", "\n").split("\n");
        List<String> values = Lists.newArrayList();
        for (String token : tokens) {
            String value = token.trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }

    public static void addActionListeners(AbstractButton button, ActionListener[] listeners) {
        for (ActionListener listener : listeners) {
            button.addActionListener(listener);
        }
    }

    public static boolean setLookAndFeel(String lookAndFeel) {
        try {
            UIManager.setLookAndFeel(lookAndFeel);
            return true;
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to set look and feel to " + lookAndFeel, e);
            return false;
        }
    }

    public static void setSwingProperties(String appName) {
        UIManager.getDefaults().put("SplitPane.border", BorderFactory.createEmptyBorder());
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        System.setProperty("apple.laf.useScreenMenuBar", "true");
    }
}
