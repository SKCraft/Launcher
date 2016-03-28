/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.statistics;

import com.skcraft.launcher.Instance;
import com.skcraft.launcher.swing.InstanceTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import nz.co.lolnet.james137137.FeedbackManager;
import nz.co.lolnet.james137137.HelpAndSupport;
import nz.co.lolnet.james137137.LauncherGobalSettings;
import org.json.simple.JSONObject;

/**
 *
 * @author James
 */
public class MetaData implements Runnable {

    static long maxMemory;
    static long currentMemory;
    static int numberOfCores;

    private static JPanel panel;
    private static JOptionPane optionPane;
    private static JEditorPane editorPane;

    public MetaData() {
        start();
    }

    private void start() {
        Thread t = new Thread(this);
        t.start();
    }

    public void setup() {

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            if (method.getName().startsWith("getTotalPhysicalMemorySize")
                    && Modifier.isPublic(method.getModifiers())) {

                try {
                    maxMemory = (Long) method.invoke(operatingSystemMXBean);
                } catch (Exception e) {
                }
            } else if (method.getName().startsWith("getFreePhysicalMemorySize")
                    && Modifier.isPublic(method.getModifiers())) {

                try {
                    currentMemory = (Long) method.invoke(operatingSystemMXBean);
                } catch (Exception e) {
                }
            }
        } // for

        numberOfCores = Runtime.getRuntime().availableProcessors();

    }

    private JSONObject getMetaData() {

        JSONObject serverInfo = new JSONObject();
        serverInfo.put("currentMemory", currentMemory);
        serverInfo.put("maxMemory", maxMemory);
        serverInfo.put("numberOfCores", numberOfCores);
        serverInfo.put("userNames", FeedbackManager.usernamesToString());

        for (Instance instance : InstanceTableModel.instanceTableModel.instances.getInstances()) {
            String launchCount = LauncherGobalSettings.get("InstanceLaunchCount_" + instance.getTitle());
            serverInfo.put(instance.getTitle(), launchCount);
            System.out.println(instance.toString());
        }

        return serverInfo;
    }

    @Override
    public void run() {
        try {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(MetaData.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (getPermission()) {
                setup();
                LauncherStatistics.sendMetrics(getMetaData());
            }

        } catch (Exception ex) {
            Logger.getLogger(MetaData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean getPermission() {
        LauncherGobalSettings.put("Metrics-OpOut", "");
        if (LauncherGobalSettings.get("Metrics-OpOut").length() == 0) {
            Object[] options = {"Yes", "No", "More Info"};
            int result = JOptionPane.showOptionDialog(null,
                    "Would you like to send Metrics data to lolnet?"
                    + "\nThis will improve the Lolnet launcher "
                    + "and help the lolnet Team \ndecide "
                    + "what modpacks we should host next", "Metrics",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    null);
            switch (result) {
                case 1:
                    LauncherGobalSettings.put("Metrics-OpOut", "true");
                    break;
                case 0:
                    LauncherGobalSettings.put("Metrics-OpOut", "false");
                    break;
                case 2:
                    boolean openURL = HelpAndSupport.openURL("https://www.google.com");
                    break;
                default:
                    break;
            }
        }
        return !LauncherGobalSettings.get("Metrics-OpOut").equals("true");
    }

}
