/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;

import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.Account;
import com.skcraft.launcher.auth.AccountList;
import com.skcraft.launcher.swing.ActionListeners;
import com.skcraft.launcher.swing.LinedBoxPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import nz.co.lolnet.statistics.ThreadSendFeedback;

/**
 *
 * @author James
 */
public class FeedbackManager implements ActionListener{

    private static boolean loaded = false;
    private static HashMap<String, String> usernames = new HashMap<>();
    private static AccountList accounts;

    public static void setupAccountList() {
        if (loaded) {
            return;
        }
        loaded = true;
        LoadUsernames();
        accounts = Launcher.instance.getAccounts();
        for (Account account : accounts.getAccounts()) {
            if (!usernames.containsKey(account.getId())) {
                usernames.put(account.getId(), "NULL");
            }
        }
    }

    private static String usernamesToString() {
        String data = "";
        for (String key : usernames.keySet()) {
            data += key + "~~~~" + usernames.get(key) + ",";
        }
        return data;
    }

    public static void saveUsernames() {

        LauncherGobalSettings.put("LolnetLauncherUserNameMap", usernamesToString());
    }

    public static void LoadUsernames() {
        String data = LauncherGobalSettings.get("LolnetLauncherUserNameMap");
        String[] spilt = data.split(",");
        for (String string : spilt) {
            String[] spilt2 = string.split("~~~~");
            if (spilt2.length == 2) {
                addUser(spilt2[0], spilt2[1], false);
            }
        }
    }

    public static void addUser(String email_username, String playerName, boolean save) {
        usernames.put(email_username, playerName);
        if (save) {
            saveUsernames();
        }
    }

    public static void openWindow() {
        LinedBoxPanel pPButtonsPanel = new LinedBoxPanel(true).fullyPadded();
        JDialog frame = new JDialog();
        frame.setTitle("Send feedback or suggestions");
        JButton pPSendButton = new JButton("Send");
        JButton pPCloseButton = new JButton("Cancel");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(new Dimension(400, 200));
        frame.setResizable(false);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
        JTextField field = new JTextField();
        field.setBorder(BorderFactory.createLineBorder(Color.black));
        final JTextArea area = new JTextArea(20, 80);
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        area.setEditable(true);

        pPButtonsPanel.add(pPSendButton, BorderLayout.WEST);
        pPButtonsPanel.addGlue();
        pPButtonsPanel.add(pPCloseButton, BorderLayout.EAST);

        frame.add(pPButtonsPanel, BorderLayout.SOUTH);
        frame.add(scrollPane);
        frame.setVisible(true);

        pPCloseButton.addActionListener(ActionListeners.dispose(frame));

        pPSendButton.addActionListener(ActionListeners.dispose(frame));
        pPSendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (area.getText().length() > 2) {
                    new ThreadSendFeedback(area.getText(), usernamesToString());
                    JOptionPane.showMessageDialog(null, "Thank you for your feedback.", "Feedback sent.", JOptionPane.INFORMATION_MESSAGE);
                }
                
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        openWindow();
    }

}
