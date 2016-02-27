/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */
package com.skcraft.launcher.swing;

import com.skcraft.launcher.Instance;
import com.skcraft.launcher.InstanceList;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.util.SharedLocale;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;

import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.util.HashMap;
import javax.imageio.ImageIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.parser.ParseException;

public class InstanceTableModel extends AbstractTableModel {

    private static HashMap<String, ImageIcon> imageIconMap = new HashMap<>();
    private static HashMap<String, String> instanceInfo = new HashMap<>();
    boolean firstTimeRun = false;
    private static HashMap<String, Integer> playerCount = new HashMap<>();
    private final InstanceList instances;
    private final ImageIcon instanceIcon;
    private final ImageIcon customInstanceIcon;
    private final ImageIcon downloadIcon;
    public static InstanceTableModel instanceTableModel;

    public InstanceTableModel(InstanceList instances) {
        instanceTableModel = this;
        this.instances = instances;
        instanceIcon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "instance_icon.png")
                .getScaledInstance(64, 64, Image.SCALE_SMOOTH));
        customInstanceIcon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "custom_instance_icon.png")
                .getScaledInstance(64, 64, Image.SCALE_SMOOTH));
        downloadIcon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "download_icon.png")
                .getScaledInstance(14, 14, Image.SCALE_SMOOTH));
        new ThreadPlayerCount();
    }

    public void update() {
        instances.sort();
        fireTableDataChanged();
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "";
            case 1:
                return SharedLocale.tr("launcher.modpackColumn");
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return ImageIcon.class;
            case 1:
                return String.class;
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                instances.get(rowIndex).setSelected((boolean) (Boolean) value);
                break;
            case 1:
            default:
                break;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return false;
            case 1:
                return false;
            default:
                return false;
        }
    }

    @Override
    public int getRowCount() {
        return instances.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Instance instance;
        switch (columnIndex) {
            case 0:
                instance = instances.get(rowIndex);
                if (!instance.isLocal()) {
                    return getDownloadIcon(instance);
                } else if (instance.getManifestURL() != null) {
                    return getInstanceIcon(instance);
                } else {
                    return getCustomInstanceIcon(instance);
                }
            case 1:
                instance = instances.get(rowIndex);
                return "<html>" + "<p><font size=\"4\"><b>" + SwingHelper.htmlEscape(instance.getTitle()) + "</b></font> " + getNumberOfPlayers(instance) + "</p>" + "<p>" + getAddendum(instance) + "</p>"
                        + "<p><font size=\"3\">" + getInstanceInfomation(instance) + "</font></p>" + "</html>";
            default:
                return null;
        }
    }

    public static String getPlayerCountFromServer(Instance instance, int server) throws UnsupportedEncodingException, IOException, ParseException {
        String data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(instance.getTitle(), "UTF-8");
        URL url = new URL("https://www.lolnet.co.nz/api/v1.0/lolstats/getPlayerCount" + server + ".php");
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(data);
        wr.flush();
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String output = rd.readLine();
        wr.close();
        rd.close();
        return output;
    }

    private HashMap<String, Integer> getNumberOfPlayers() {
        return playerCount;
    }

    private String getNumberOfPlayers(Instance instance) {
        int count = 0;
        if (getNumberOfPlayers().get(instance.getTitle()) != null) {
            count = getNumberOfPlayers().get(instance.getTitle());
        }

        if (count > 0) {
            return "<font size=\"4\" color=\"Green\">(Players Online: " + count + ")</font>";
        }
        if (count < 0) {
            return "<font size=\"4\" color=\"Red\">(Server Offline)</font>";
        }

        return "";
    }

    private String getAddendum(Instance instance) {
        if (!instance.isLocal()) {
            return " <span style=\"color: #cccccc\">" + SharedLocale.tr("launcher.notInstalledHint") + "</span>";
        } else if (!instance.isInstalled()) {
            return " <span style=\"color: red\">" + SharedLocale.tr("launcher.requiresUpdateHint") + "</span>";
        } else if (instance.isUpdatePending()) {
            return " <span style=\"color: #3758DB\">" + SharedLocale.tr("launcher.updatePendingHint") + "</span>";
        } else {
            return "";
        }
    }

    public static String getInstanceInfomation(Instance instance) {
        String line = instanceInfo.get(instance.getTitle());

        if (line == null) {
            line = "";
            try {
                URL url;
                url = new URL(Launcher.modPackURL + "instanceicon/" + instance.getTitle().replaceAll(" ", "_") + "/info.php");
                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                if (huc.getResponseCode() == 404) {
                    url = null;
                }
                if (url != null) {
                    URLConnection conn = url.openConnection();
                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.flush();

                    // Get the response
                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    while ((line = rd.readLine()) != null) {
                        instanceInfo.put(instance.getTitle(), line);
                        return line;
                    }
                    wr.close();
                    rd.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            instanceInfo.put(instance.getTitle(), line);
        }
        return line;
    }

    private ImageIcon getDownloadIcon(Instance instance) {
        //if (true) return downloadIcon;
        ImageIcon icon = imageIconMap.get(instance.getTitle() + "_" + "DownloadIcon");
        if (icon == null) {
            BufferedImage image;
            try {
                URL url = new URL(Launcher.modPackURL + "instanceicon/" + instance.getTitle().replaceAll(" ", "_") + "/download_icon.png");
                url = Launcher.checkURL(url);
                if (exists(url.toString())) {
                    image = ImageIO.read(url);
                    icon = new ImageIcon(image.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
                } else {
                    icon = downloadIcon;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            imageIconMap.put(instance.getTitle() + "_" + "DownloadIcon", icon);
        }
        return icon;

    }

    private ImageIcon getInstanceIcon(Instance instance) {
        ImageIcon icon = imageIconMap.get(instance.getTitle() + "_" + "InstanceIcon");
        if (icon == null) {
            BufferedImage image;
            try {
                URL url = new URL(Launcher.modPackURL + "instanceicon/" + instance.getTitle().replaceAll(" ", "_") + "/instance_icon.png");
                url = Launcher.checkURL(url);
                if (exists(url.toString())) {
                    image = ImageIO.read(url);
                    icon = new ImageIcon(image.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
                } else {
                    icon = instanceIcon;
                }
            } catch (Exception ex) {
                icon = downloadIcon;
                ex.printStackTrace();
            }
            imageIconMap.put(instance.getTitle() + "_" + "InstanceIcon", icon);
        }

        return icon;
    }

    private ImageIcon getCustomInstanceIcon(Instance instance) {
        ImageIcon icon = imageIconMap.get(instance.getTitle() + "_" + "CustomInstanceIcon");
        if (icon == null) {
            BufferedImage image;
            try {
                URL url = new URL(Launcher.modPackURL + "instanceicon/" + instance.getTitle().replaceAll(" ", "_") + "/custom_instance_icon.png");
                url = Launcher.checkURL(url);
                if (exists(url.toString())) {
                    image = ImageIO.read(url);
                    icon = new ImageIcon(image.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
                } else {
                    icon = customInstanceIcon;
                }
            } catch (Exception ex) {
                icon = customInstanceIcon;
                ex.printStackTrace();
            }
            imageIconMap.put(instance.getTitle() + "_" + "CustomInstanceIcon", icon);
        }

        return customInstanceIcon;
    }

    public static boolean exists(String URLName) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con
                    = (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private ImageIcon generateIconFromTemplate(Instance instance) {
        String text = instance.getTitle();

        /*
           Because font metrics is based on a graphics context, we need to create
           a small, temporary image so we can ascertain the width and height
           of the final image
         */
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        Font font = new Font("Arial", Font.PLAIN, 48);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(text);
        int height = fm.getHeight();
        g2d.dispose();

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, 0, fm.getAscent());
        g2d.dispose();

        return new ImageIcon(img);
    }

    private static class ThreadPlayerCount implements Runnable {

        public ThreadPlayerCount() {
            start();
        }

        private void start() {
            Thread t = new Thread(this);
            t.start();
        }

        @Override
        public void run() {
            while (InstanceTableModel.instanceTableModel.instances.size() <= 0) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(InstanceTableModel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            while (true) {

                for (int i = 0; i < InstanceTableModel.instanceTableModel.instances.size(); i++) {
                    boolean isOnline = false;
                    Instance instance = InstanceTableModel.instanceTableModel.instances.get(i);
                    int count = 0;
                    for (int j = 1; j <= 2; j++) {

                        try {
                            int num = Integer.parseInt(getPlayerCountFromServer(instance, j));
                            if (num >= 0) {
                                count += num;
                                isOnline = true;
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(InstanceTableModel.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ParseException ex) {
                            Logger.getLogger(InstanceTableModel.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                    if (isOnline) {
                        playerCount.put(instance.getTitle(), count);
                    } else {
                        playerCount.put(instance.getTitle(), -1);
                    }
                }
                InstanceTableModel.instanceTableModel.update();
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(InstanceTableModel.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }

}
