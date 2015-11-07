/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

/**
 *
 * @author James
 */
public class ThreadLolnetPingWindow implements Runnable {

    JScrollPane pane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    private static JFrame frame = null;
    JLabel jLabel;

    public ThreadLolnetPingWindow() {
        if (frame != null && frame.isVisible()) {
            return;

        }
        frame = new JFrame();
        jLabel = new JLabel();
        
        //frame.getContentPane().add(jLabel);
        
        start();
    }

    private void start() {
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {

        reloadPicture();
        
        frame.getContentPane().setBackground(Color.BLACK);
        try {
            frame.setSize(new Dimension(getImage().getWidth(null) + 40, getImage().getHeight(null)+50));
        } catch (IOException ex) {
            Logger.getLogger(ThreadLolnetPingWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2 - 50 , dim.height / 2 - frame.getSize().height / 2);
        frame.setVisible(true);
        while (frame != null && jLabel != null && frame.isVisible() && jLabel.isVisible()) {

            reloadPicture();
            try {
                Thread.sleep(60*5 * 1000);                
            } catch (InterruptedException ex) {
                Logger.getLogger(ThreadLolnetPingWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (frame != null) {
            frame.setVisible(false);
        }

    }

    private void reloadPicture() {
        try {
            ImageIcon imageIcon = new ImageIcon(ImageIO.read(new URL("https://www.lolnet.co.nz/LolnetServerStatus/FullServers.png")));
            jLabel.setIcon(imageIcon);
            pane = new JScrollPane(new JLabel(imageIcon));
            frame.setContentPane(pane);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ThreadLolnetPingWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ThreadLolnetPingWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private BufferedImage getImage() throws MalformedURLException, IOException {
        return ImageIO.read(new URL("https://www.lolnet.co.nz/LolnetServerStatus/FullServers.png"));
    }

}
