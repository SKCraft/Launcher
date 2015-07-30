/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import com.skcraft.launcher.LauncherUtils;
import com.skcraft.launcher.util.LimitLinesDocumentListener;
import com.skcraft.launcher.util.SimpleLogFormatter;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * A simple message log.
 */
public class MessageLog extends JPanel {

    private static final Logger rootLogger = Logger.getLogger("");
    
    private final int numLines;
    private final boolean colorEnabled;
    
    protected JTextComponent textComponent;
    protected Document document;

    private Handler loggerHandler;
    protected final SimpleAttributeSet defaultAttributes = new SimpleAttributeSet();
    protected final SimpleAttributeSet highlightedAttributes;
    protected final SimpleAttributeSet errorAttributes;
    protected final SimpleAttributeSet infoAttributes;
    protected final SimpleAttributeSet debugAttributes;

    public MessageLog(int numLines, boolean colorEnabled) {
        this.numLines = numLines;
        this.colorEnabled = colorEnabled;
        
        this.highlightedAttributes = new SimpleAttributeSet();
        StyleConstants.setForeground(highlightedAttributes, new Color(0xFF7F00));
        
        this.errorAttributes = new SimpleAttributeSet();
        StyleConstants.setForeground(errorAttributes, new Color(0xFF0000));
        this.infoAttributes = new SimpleAttributeSet();
        this.debugAttributes = new SimpleAttributeSet();

        setLayout(new BorderLayout());
        
        initComponents();
    }

    private void initComponents() {
        if (colorEnabled) {
            JTextPane text = new JTextPane() {
                @Override
                public boolean getScrollableTracksViewportWidth() {
                    return true;
                }
            };
            this.textComponent = text;
        } else {
            JTextArea text = new JTextArea();
            this.textComponent = text;
            text.setLineWrap(true);
            text.setWrapStyleWord(true);
        }

        textComponent.setFont(new JLabel().getFont());
        textComponent.setEditable(false);
        textComponent.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);
        DefaultCaret caret = (DefaultCaret) textComponent.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        document = textComponent.getDocument();
        document.addDocumentListener(new LimitLinesDocumentListener(numLines, true));
        
        JScrollPane scrollText = new JScrollPane(textComponent);
        scrollText.setBorder(null);
        scrollText.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollText.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollText, BorderLayout.CENTER);
    }
    
    public String getPastableText() {
        String text = textComponent.getText().replaceAll("[\r\n]+", "\n");
        text = text.replaceAll("Session ID is [A-Fa-f0-9]+", "Session ID is [redacted]");
        return text;
    }

    public void clear() {
        textComponent.setText("");
    }
    
    /**
     * Log a message given the {@link javax.swing.text.AttributeSet}.
     * 
     * @param line line
     * @param attributes attribute set, or null for none
     */
    public void log(final String line, AttributeSet attributes) {
        final Document d = document;
        final JTextComponent t = textComponent;
        if (colorEnabled) {
            if (line.startsWith("(!!)")) {
                attributes = highlightedAttributes;
            }
        }
        final AttributeSet a = attributes;
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    int offset = d.getLength();
                    d.insertString(offset, line,
                            (a != null && colorEnabled) ? a : defaultAttributes);
                    t.setCaretPosition(d.getLength());
                } catch (BadLocationException ble) {
                
                }
            }
        });
    }
    
    /**
     * Get an output stream that can be written to.
     * 
     * @return output stream
     */
    public ConsoleOutputStream getOutputStream() {
        return getOutputStream((AttributeSet) null);
    }
    
    /**
     * Get an output stream with the given attribute set.
     * 
     * @param attributes attributes
     * @return output stream
     */
    public ConsoleOutputStream getOutputStream(AttributeSet attributes) {
        return new ConsoleOutputStream(attributes);
    }

    /**
     * Get an output stream using the give color.
     * 
     * @param color color to use
     * @return output stream
     */
    public ConsoleOutputStream getOutputStream(Color color) {
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setForeground(attributes, color);
        return getOutputStream(attributes);
    }
    
    /**
     * Consume an input stream and print it to the dialog. The consumer
     * will be in a separate daemon thread.
     * 
     * @param from stream to read
     */
    public void consume(InputStream from) {
        consume(from, getOutputStream());
    }

    /**
     * Consume an input stream and print it to the dialog. The consumer
     * will be in a separate daemon thread.
     * 
     * @param from stream to read
     * @param color color to use
     */
    public void consume(InputStream from, Color color) {
        consume(from, getOutputStream(color));
    }

    /**
     * Consume an input stream and print it to the dialog. The consumer
     * will be in a separate daemon thread.
     * 
     * @param from stream to read
     * @param attributes attributes
     */
    public void consume(InputStream from, AttributeSet attributes) {
        consume(from, getOutputStream(attributes));
    }
    
    /**
     * Internal method to consume a stream.
     * 
     * @param from stream to consume
     * @param outputStream console stream to write to
     */
    private void consume(InputStream from, ConsoleOutputStream outputStream) {
        final InputStream in = from;
        final PrintWriter out = new PrintWriter(outputStream, true);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                try {
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        String s = new String(buffer, 0, len);
                        System.out.print(s);
                        out.append(s);
                        out.flush();
                    }
                } catch (IOException e) {
                } finally {
                    closeQuietly(in);
                    closeQuietly(out);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Register a global logger listener.
     */
    public void registerLoggerHandler() {
        loggerHandler = new ConsoleLoggerHandler();
        rootLogger.addHandler(loggerHandler);
    }
    
    /**
     * Detach the handler on the global logger.
     */
    public void detachGlobalHandler() {
        if (loggerHandler != null) {
            rootLogger.removeHandler(loggerHandler);
            loggerHandler = null;
        }
    }

    public SimpleAttributeSet asDefault() {
        return defaultAttributes;
    }

    public SimpleAttributeSet asHighlighted() {
        return highlightedAttributes;
    }

    public SimpleAttributeSet asError() {
        return errorAttributes;
    }

    public SimpleAttributeSet asInfo() {
        return infoAttributes;
    }

    public SimpleAttributeSet asDebug() {
        return debugAttributes;
    }

    /**
     * Used to send logger messages to the console.
     */
    private class ConsoleLoggerHandler extends Handler {
        private final SimpleLogFormatter formatter = new SimpleLogFormatter();

        @Override
        public void publish(LogRecord record) {
            Level level = record.getLevel();
            Throwable t = record.getThrown();
            AttributeSet attributes = defaultAttributes;

            if (level.intValue() >= Level.WARNING.intValue()) {
                attributes = errorAttributes;
            } else if (level.intValue() < Level.INFO.intValue()) {
                attributes = debugAttributes;
            }

            log(formatter.format(record), attributes);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
    
    /**
     * Used to send console messages to the console.
     */
    private class ConsoleOutputStream extends ByteArrayOutputStream {
        private AttributeSet attributes;
        
        private ConsoleOutputStream(AttributeSet attributes) {
            this.attributes = attributes;
        }
        
        @Override
        public void flush() {
            String data = toString();
            if (data.length() == 0) return;
            log(data, attributes);
            reset();
        }
    }

}
