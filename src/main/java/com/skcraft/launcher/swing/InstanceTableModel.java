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
import java.net.URL;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class InstanceTableModel extends AbstractTableModel {

    private static HashMap<String, ImageIcon> imageIconMap = new HashMap<>();
    private final InstanceList instances;
    private final ImageIcon instanceIcon;
    private final ImageIcon customInstanceIcon;
    private final ImageIcon downloadIcon;

    public InstanceTableModel(InstanceList instances) {
        this.instances = instances;
        instanceIcon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "instance_icon.png")
                .getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        customInstanceIcon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "custom_instance_icon.png")
                .getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        downloadIcon = new ImageIcon(SwingHelper.readIconImage(Launcher.class, "download_icon.png")
                .getScaledInstance(14, 14, Image.SCALE_SMOOTH));
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
                return "<html>" + SwingHelper.htmlEscape(instance.getTitle()) + getAddendum(instance) + "</html>";
            default:
                return null;
        }
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

    private ImageIcon getDownloadIcon(Instance instance) {
        //if (true) return downloadIcon;
        ImageIcon icon = imageIconMap.get(instance.getTitle() + "_" + "DownloadIcon");
        if (icon == null) {
            BufferedImage image;
            try {
                URL url = new URL("https://www.lolnet.co.nz/modpack/instanceicon/" + instance.getTitle().replaceAll(" ", "_") + "/download_icon.png");
                url = Launcher.checkURL(url);
                if (exists(url.toString())) {
                    image = ImageIO.read(url);
                    icon = new ImageIcon(image.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
                }
                else
                {
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
                URL url = new URL("https://www.lolnet.co.nz/modpack/instanceicon/" + instance.getTitle().replaceAll(" ", "_") + "/instance_icon.png");
                url = Launcher.checkURL(url);
                if (exists(url.toString())) {
                    image = ImageIO.read(url);
                    icon = new ImageIcon(image.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
                }
                else
                {
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
                URL url = new URL("https://www.lolnet.co.nz/modpack/instanceicon/" + instance.getTitle().replaceAll(" ", "_") + "/custom_instance_icon.png");
                url = Launcher.checkURL(url);
                if (exists(url.toString())) {
                    image = ImageIO.read(url);
                    icon = new ImageIcon(image.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
                }
                else
                {
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

}
