/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;

/**
 *
 * @author James
 */
import com.skcraft.launcher.swing.SwingHelper;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static javafx.concurrent.Worker.State.FAILED;
import javafx.stage.FileChooser;

public class SimpleSwingBrowser extends JFrame {

    public static JFXPanel jfxPanel;
    private static WebEngine engine;

    private final JPanel panel = new JPanel(new BorderLayout());
    private final JLabel lblStatus = new JLabel();

    private JTextField txtURL = new JTextField();

    public SimpleSwingBrowser() {
        super();
        initComponents();
    }

    public SimpleSwingBrowser(JTextField txtURL) {
        
        super();
        jfxPanel = new JFXPanel();
        this.txtURL = txtURL;
        this.jfxPanel = jfxPanel;
        initComponents();
    }

    private void initComponents() {
        createScene();

        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadURL(txtURL.getText());
            }
        };
        pack();

    }

    private void createScene() {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                final WebView view = new WebView();
                engine = view.getEngine();

                engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                    @Override
                    public void handle(final WebEvent<String> event) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                lblStatus.setText(event.getData());
                            }
                        });
                    }
                });

                engine.locationProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                txtURL.setText(newValue);
                            }
                        });
                    }
                });
                
                engine.locationProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override 
                            public void run() {
                                txtURL.setText(newValue);
                            }
                        });
                    }
                });

                engine.getLoadWorker()
                        .exceptionProperty()
                        .addListener(new ChangeListener<Throwable>() {

                            public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
                                if (engine.getLoadWorker().getState() == FAILED) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            JOptionPane.showMessageDialog(
                                                    panel,
                                                    (value != null)
                                                            ? engine.getLocation() + "\n" + value.getMessage()
                                                            : engine.getLocation() + "\nUnexpected error.",
                                                    "Loading error...",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                    });
                                }
                            }
                        });

                jfxPanel.setScene(new Scene(view));

                // monitor the location url, and if it is a pdf file, then create a pdf viewer for it, if it is downloadable, then download it.
                view.getEngine().locationProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observableValue, String oldLoc, String newLoc) {
                        String downloadableExtension = null;  // todo I wonder how to find out from WebView which documents it could not process so that I could trigger a save as for them?
                        String[] downloadableExtensions = {".doc", ".xls", ".zip", ".tgz", ".jar", ".exe"};
                        for (String ext : downloadableExtensions) {
                            if (newLoc.endsWith(ext)) {
                                downloadableExtension = ext;
                                break;
                            }
                        }
                        if (downloadableExtension != null) {
                            // create a file save option for performing a download.
                            FileChooser chooser = new FileChooser();
                            chooser.setTitle("Save " + newLoc);
                            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Downloadable File", downloadableExtension));
                            int filenameIdx = newLoc.lastIndexOf("/") + 1;
                            if (filenameIdx != 0) {
                                File saveFile = chooser.showSaveDialog(view.getScene().getWindow());

                                if (saveFile != null) {
                                    BufferedInputStream is = null;
                                    BufferedOutputStream os = null;
                                    try {
                                        is = new BufferedInputStream(new URL(newLoc).openStream());
                                        os = new BufferedOutputStream(new FileOutputStream(saveFile));
                                        int b = is.read();
                                        while (b != -1) {
                                            os.write(b);
                                            b = is.read();
                                        }
                                    } catch (FileNotFoundException e) {
                                        System.out.println("Unable to save file: " + e);
                                    } catch (MalformedURLException e) {
                                        System.out.println("Unable to save file: " + e);
                                    } catch (IOException e) {
                                        System.out.println("Unable to save file: " + e);
                                    } finally {
                                        try {
                                            if (is != null) {
                                                is.close();
                                            }
                                        } catch (IOException e) {
                                            /**
                                             * no action required.
                                             */
                                        }
                                        try {
                                            if (os != null) {
                                                os.close();
                                            }
                                        } catch (IOException e) {
                                            /**
                                             * no action required.
                                             */
                                        }
                                    }
                                }

                                // todo provide feedback on the save function and provide a download list and download list lookup.
                            }
                        }
                    }
                });
            }
        });
    }

    public static void loadURL(final String url) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String tmp = toURL(url);

                if (tmp == null) {
                    tmp = toURL("http://" + url);
                }

                engine.load(tmp);
            }
        });
    }

    private static String toURL(String str) {
        try {
            return new URL(str).toExternalForm();
        } catch (MalformedURLException exception) {
            return null;
        }
    }

    public JSplitPane splitPane(JScrollPane instanceScroll) {
        return new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, instanceScroll, jfxPanel);
    }
}
