/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;

import com.skcraft.launcher.Instance;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.swing.InstanceTableModel;
import static com.skcraft.launcher.swing.InstanceTableModel.exists;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author James
 */
public class ThreadInstanceIconHandler implements Runnable {

    Instance instance;
    int rowIndex;
    String type;

    public ThreadInstanceIconHandler(Instance instance, int rowIndex, String type) {
        this.instance = instance;
        this.rowIndex = rowIndex;
        this.type = type;
        HttpThreadPool.add(this);
    }

    @Override
    public void run() {
        switch (type) {
            case "CustomInstanceIcon": {
                ImageIcon icon;
                BufferedImage image;
                try {
                    URL url = new URL(Launcher.modPackURL + "instanceicon/" + instance.getTitle().replaceAll(" ", "_") + "/custom_instance_icon.png");
                    url = Launcher.checkURL(url);
                    if (exists(url.toString())) {
                        image = ImageIO.read(url);
                        icon = new ImageIcon(image.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
                    } else {
                        return;
                    }
                } catch (Exception ex) {
                    return;
                }
                InstanceTableModel.imageIconMap.put(instance.getTitle() + "_" + "CustomInstanceIcon", icon);
                InstanceTableModel.instanceTableModel.setValueAt(icon, rowIndex, 1);
                InstanceTableModel.instanceTableModel.update(false);
                break;
            }
            case "InstanceIcon": {
                ImageIcon icon;
                BufferedImage image;
                try {
                    URL url = new URL(Launcher.modPackURL + "instanceicon/" + instance.getTitle().replaceAll(" ", "_") + "/instance_icon.png");
                    url = Launcher.checkURL(url);
                    if (exists(url.toString())) {
                        image = ImageIO.read(url);
                        icon = new ImageIcon(image.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
                    } else {
                        return;
                    }
                } catch (Exception ex) {
                    return;
                }
                InstanceTableModel.imageIconMap.put(instance.getTitle() + "_" + "InstanceIcon", icon);
                InstanceTableModel.instanceTableModel.setValueAt(icon, rowIndex, 1);
                InstanceTableModel.instanceTableModel.update(false);
                break;
            }
            case "DownloadIcon":
                ImageIcon icon;
                BufferedImage image;
                try {
                    URL url = new URL(Launcher.modPackURL + "instanceicon/" + instance.getTitle().replaceAll(" ", "_") + "/download_icon.png");
                    url = Launcher.checkURL(url);
                    if (exists(url.toString())) {
                        image = ImageIO.read(url);
                        icon = new ImageIcon(image.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
                    } else {
                        return;
                    }
                } catch (Exception ex) {
                    return;
                }
                InstanceTableModel.imageIconMap.put(instance.getTitle() + "_" + "DownloadIcon", icon);
                InstanceTableModel.instanceTableModel.setValueAt(icon, rowIndex, 1);
                InstanceTableModel.instanceTableModel.update(false);
                break;
            default:
                break;
        }
    }

}
