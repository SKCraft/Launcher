/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import com.skcraft.launcher.LauncherUtils;
import lombok.extern.java.Log;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;

import static com.skcraft.launcher.LauncherUtils.checkInterrupted;

@Log
public final class WebpagePanel extends JPanel {
    
    private final WebpagePanel self = this;
    
    private URL url;
    private boolean activated;
    private JEditorPane documentView;
    private JScrollPane documentScroll;
    private JProgressBar progressBar;
    private Thread thread;
    private Border browserBorder;
    
    public static WebpagePanel forURL(URL url, boolean lazy) {
        return new WebpagePanel(url, lazy);
    }
    
    public static WebpagePanel forHTML(String html) {
        return new WebpagePanel(html);
    }

    private WebpagePanel(URL url, boolean lazy) {
        this.url = url;
        
        setLayout(new BorderLayout());
        
        if (lazy) {
            setPlaceholder();
        } else {
            setDocument();
            fetchAndDisplay(url);
        }
    }

    private WebpagePanel(String text) {
        this.url = null;
        
        setLayout(new BorderLayout());
        
        setDocument();
        setDisplay(text, null);
    }
    
    public WebpagePanel(boolean lazy) {
        this.url = null;
        
        setLayout(new BorderLayout());

        if (lazy) {
            setPlaceholder();
        } else {
            setDocument();
        }
    }

    public Border getBrowserBorder() {
        return browserBorder;
    }

    public void setBrowserBorder(Border browserBorder) {
        synchronized (this) {
            this.browserBorder = browserBorder;
            if (documentScroll != null) {
                documentScroll.setBorder(browserBorder);
            }
        }
    }

    private void setDocument() {
        activated = true;
        
        JLayeredPane panel = new JLayeredPane();
        panel.setLayout(new WebpageLayoutManager());
        
        documentView = new JEditorPane();
        documentView.setOpaque(false);
        documentView.setBorder(null);
        documentView.setEditable(false);
        documentView.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if (e.getURL() != null) {
                        SwingHelper.openURL(e.getURL(), self);
                    }
                }
            }
        });

        documentScroll = new JScrollPane(documentView);
        documentScroll.setOpaque(false);
        panel.add(documentScroll, new Integer(1));
        documentScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        synchronized (this) {
            if (browserBorder != null) {
                documentScroll.setBorder(browserBorder);
            }
        }
        
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        panel.add(progressBar, new Integer(2));

        SwingHelper.removeOpaqueness(this);
        SwingHelper.removeOpaqueness(documentView);
        SwingHelper.removeOpaqueness(documentScroll);
        
        add(panel, BorderLayout.CENTER);
    }
    
    private void setPlaceholder() {
        activated = false;
        
        JLayeredPane panel = new JLayeredPane();
        panel.setBorder(new CompoundBorder(
                BorderFactory.createEtchedBorder(), BorderFactory
                        .createEmptyBorder(4, 4, 4, 4)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        final JButton showButton = new JButton("Load page");
        showButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        showButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showButton.setVisible(false);
                setDocument();
                fetchAndDisplay(url);
            }
        });
        
        // Center the button vertically.
        panel.add(new Box.Filler(
                new Dimension(0, 0),
                new Dimension(0, 0),
                new Dimension(1000, 1000)));
        panel.add(showButton);
        panel.add(new Box.Filler(
                new Dimension(0, 0),
                new Dimension(0, 0),
                new Dimension(1000, 1000)));
        
        add(panel, BorderLayout.CENTER);
    }
    
    /**
     * Browse to a URL.
     * 
     * @param url the URL
     * @param onlyChanged true to only browse if the last URL was different
     * @return true if only the URL was changed
     */
    public boolean browse(URL url, boolean onlyChanged) {
        if (onlyChanged && this.url != null && this.url.equals(url)) {
            return false;
        }
        
        this.url = url;
        
        if (activated) {
            fetchAndDisplay(url);
        }
        
        return true;
    }

    /**
     * Update the page. This has to be run in the Swing event thread.
     * 
     * @param url the URL
     */
    private synchronized void fetchAndDisplay(URL url) {
        if (thread != null) {
            thread.interrupt();
        }
        
        progressBar.setVisible(true);
        
        thread = new Thread(new FetchWebpage(url));
        thread.setDaemon(true);
        thread.start();
    }

    private void setDisplay(String text, URL baseUrl) {
        progressBar.setVisible(false);
        documentView.setContentType("text/html");
        HTMLDocument document = (HTMLDocument) documentView.getDocument();
        
        // Clear existing styles
        Enumeration<?> e = document.getStyleNames();
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            document.removeStyle((String) o);
        }
        
        document.setBase(baseUrl);
        documentView.setText(text);
        
        documentView.setCaretPosition(0);
    }

    private void setError(String text) {
        progressBar.setVisible(false);
        documentView.setContentType("text/plain");
        documentView.setText(text);
        documentView.setCaretPosition(0);
    }
    
    private class FetchWebpage implements Runnable {
        private URL url;
        
        public FetchWebpage(URL url) {
            this.url = url;
        }
        
        @Override
        public void run() {
            HttpURLConnection conn = null;

            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(false);
                conn.setReadTimeout(5000);

                conn.connect();

                checkInterrupted();

                if (conn.getResponseCode() != 200) {
                    throw new IOException(
                            "Did not get expected 200 code, got "
                                    + conn.getResponseCode());
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(),
                                "UTF-8"));

                StringBuilder s = new StringBuilder();
                char[] buf = new char[1024];
                int len = 0;
                while ((len = reader.read(buf)) != -1) {
                    s.append(buf, 0, len);
                }
                String result = s.toString();
                
                checkInterrupted();

                setDisplay(result, LauncherUtils.concat(url, ""));
            } catch (IOException e) {
                if (Thread.interrupted()) {
                    return;
                }
                
                log.log(Level.WARNING, "Failed to fetch page", e);
                setError("Failed to fetch page: " + e.getMessage());
            } catch (InterruptedException e) {
            } finally {
                if (conn != null)
                    conn.disconnect();
                conn = null;
            }
        }
    }

}
