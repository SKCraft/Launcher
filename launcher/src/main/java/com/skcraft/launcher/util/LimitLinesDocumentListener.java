/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.util;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

/**
 * From http://tips4java.wordpress.com/2008/10/15/limit-lines-in-document/
 * 
 * @author Rob Camick
 */
public class LimitLinesDocumentListener implements DocumentListener {
    private int maximumLines;
    private boolean isRemoveFromStart;
    private volatile boolean isRemoving;

    /**
     * Specify the number of lines to be stored in the Document. Extra lines
     * will be removed from the start or end of the Document, depending on
     * the boolean value specified.
     * 
     * @param maximumLines number of lines
     * @param isRemoveFromStart true to remove from the start
     */
    public LimitLinesDocumentListener(int maximumLines,
                                      boolean isRemoveFromStart) {
        setLimitLines(maximumLines);
        this.isRemoveFromStart = isRemoveFromStart;
        this.isRemoving = false;
    }

    /**
     * Set the maximum number of lines to be stored in the Document
     * 
     * @param maximumLines number of lines
     */
    public void setLimitLines(int maximumLines) {
        if (maximumLines < 1) {
            throw new IllegalArgumentException("Maximum lines must be greater than 0");
        }

        this.maximumLines = maximumLines;
    }

    @Override
    public void insertUpdate(final DocumentEvent e) {
        // Changes to the Document can not be done within the listener
        // so we need to add the processing to the end of the EDT

        if (!this.isRemoving) {
            this.isRemoving = true;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    removeLines(e);
                }
            });
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    private void removeLines(DocumentEvent e) {
        // The root Element of the Document will tell us the total number
        // of line in the Document.

        try {
            Document document = e.getDocument();
            Element root = document.getDefaultRootElement();
            int excess = root.getElementCount() - maximumLines;

            if (excess > 0) {
                if (isRemoveFromStart) {
                    removeFromStart(document, root, excess);
                } else {
                    removeFromEnd(document, root);
                }
            }
        } finally {
            this.isRemoving = false;
        }
    }

    private void removeFromStart(Document document, Element root, int excess) {
        Element line = root.getElement(excess - 1);
        int end = line.getEndOffset();

        try {
            document.remove(0, end);
        } catch (BadLocationException ble) {
            System.out.println(ble);
        }
    }

    private void removeFromEnd(Document document, Element root) {
        // We use start minus 1 to make sure we remove the newline
        // character of the previous line

        Element line = root.getElement(maximumLines);
        int start = line.getStartOffset();
        int end = root.getEndOffset();

        try {
            document.remove(start - 1, end - start);
        } catch (BadLocationException ble) {
            System.out.println(ble);
        }
    }
}
