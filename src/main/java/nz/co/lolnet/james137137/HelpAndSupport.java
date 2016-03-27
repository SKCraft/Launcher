/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;

import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.dialog.LauncherFrame;
import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author James
 */
public class HelpAndSupport {

    public static boolean openURL(String toURL) {
        try {
            URL url = new URL(toURL);
            Desktop.getDesktop().browse(url.toURI());
            return true;
        } catch (URISyntaxException | IOException ex) {
            Logger.getLogger(HelpAndSupport.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

    }

    public static void Start() {
        LauncherFrame.instance.showModPackInstances(false);
        //Custom button text
        Object[] options = {"LolnetLauncher", "My account", "Restart Launcher"};
        int answer = JOptionPane.showOptionDialog(null,
                "What can I help you with?",
                "LolnetLauncher Help",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                null);
        switch (answer) {
            case 0:
                LauncherHelp();
                break;
            case 1:
                AccountHelp();
                break;
            case 2:
                Launcher.restartLauncher();
                break;
            default:
                break;
        }
    }

    private static void LauncherHelp() {
        Object[] options = {"I found an error/bug", "I have a suggestion for the Launcher", "I would like to submit a modpack", "lastest SnapShot please"};
        int answer = JOptionPane.showOptionDialog(null,
                "What subject on LolnetLauncher",
                "LolnetLauncher Help",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                null);
        if (answer == 0) {
            sendToLauncherBugHelp();
        } else if (answer == 1) {
            sendToLauncherRequest();
        } else if (answer == 2) {
            goToNewModPackSummition();
        } else if (answer == 3) {
            getLatestSnapshot();
        }
    }

    private static void sendToLauncherBugHelp() {
        Object[] options = {"Take me there", "Make a post now", "Cancel"};
        int answer = JOptionPane.showOptionDialog(null,
                "It is suggested that you read the FAQ before posting.",
                "LolnetLauncher Help",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                null);
        if (answer == 0) {
            openURL("https://www.lolnet.co.nz/forum/viewtopic.php?f=135&t=5684");
        } else if (answer == 1) {
            openURL("https://www.lolnet.co.nz/forum/posting.php?mode=post&f=135");
        }
    }

    private static void sendToLauncherRequest() {
        Object[] options = {"Take me there", "Cancel"};
        int answer = JOptionPane.showOptionDialog(null,
                "We are always greatful for suggestion and feedback.\nYou can do so by making a post on the forum",
                "LolnetLauncher Help",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                null);
        if (answer == 0) {
            openURL("https://www.lolnet.co.nz/forum/viewforum.php?f=135");
        }
    }

    private static void AccountHelp() {
        Object[] options = {"I can't login", "I forgot my password", "I don't have a username"};
        int answer = JOptionPane.showOptionDialog(null,
                "What is the problem.",
                "Account Help",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                null);
        if (answer == 0) {
            CantLogin();
        } else if (answer == 1) {
            openURL("https://help.mojang.com/customer/portal/articles/329524-change-or-forgot-password");
        } else if (answer == 2) {
            openURL("https://help.mojang.com/customer/portal/articles/928638-minecraft-usernames?b_id=5408");
        }
    }

    private static void goToNewModPackSummition() {
        try {
            URL url = new URL(Launcher.modPackURL + "modPackSummition.html");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            int responseCode = huc.getResponseCode();
            if (responseCode != 404) {
                Object[] options = {"Take me there", "Cancel"};
                int answer = JOptionPane.showOptionDialog(null,
                        "",
                        "Launcher Help",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        null);
                if (answer == 0) {
                    openURL(url.toString());
                }

            } else {
                JOptionPane.showMessageDialog(null, "Feature comming soon");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Unable to find infomation on that...", "Lolnet ModPack", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    private static void CantLogin() {
        Object[] options = {"In game", "Launcher"};
        int answer = JOptionPane.showOptionDialog(null,
                "Is this ingame or on the launcher?",
                "Account Help",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                null);
        if (answer == 0) {
            CantLogin_InGame();
        } else if (answer == 1) {
            CantLogin_InLauncher();
        }
    }

    private static void CantLogin_InGame() {
        Object[] options = {"Yes", "No", "I don't know..."};
        int answer = JOptionPane.showOptionDialog(null,
                "Are you banned?",
                "Account Help",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                null);
        if (answer == 0) {
            openURL("https://www.lolnet.co.nz/forum/viewforum.php?f=34");
        } else if (answer == 1) {
            sendToMinecraftSupportPage();
        } else if (answer == 2) {
            sendToMinecraftSupportPage();
        }
    }

    private static void CantLogin_InLauncher() {
        Object[] options = {"Yes", "No"};
        int answer = JOptionPane.showOptionDialog(null,
                "Do you have a Minecraft Premium Account",
                "Account Help",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                null);
        if (answer == 0) {
            sendToChangeMojangPassword();
        } else if (answer == 1) {
            JOptionPane.showMessageDialog(null, "Just add your ingame username and press \"Play offline\" on the bottom left", "Launcher offline mode feature", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void sendToMinecraftSupportPage() {
        Object[] options = {"Take me there", "Cancel"};
        int answer = JOptionPane.showOptionDialog(null,
                "It seems that it would be best to make a post on our forums",
                "Account Help",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                null);
        if (answer == 0) {
            openURL("https://www.lolnet.co.nz/forum/posting.php?mode=post&f=134");
        }
    }

    private static void sendToChangeMojangPassword() {
        Object[] options = {"Take me there", "Cancel"};
        int answer = JOptionPane.showOptionDialog(null,
                "If you have forgeten your password you might need to reset it.",
                "Account Help",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                null);
        if (answer == 0) {
            openURL("https://minecraft.net/resetpassword");
        }
    }

    private static void getLatestSnapshot() {

        try {
            LauncherFrame.instance.requestUpdate(new URL(downloadTextFromUrl("https://www.lolnet.co.nz/modpack/snapshot")),"Snapshot");
            LauncherGobalSettings.put("DownloadSnapShot", "");
        } catch (MalformedURLException ex) {
            LauncherGobalSettings.put("DownloadSnapShot", "true");
            JOptionPane.showMessageDialog(null, "Restart Launcher to update", "Launcher Update", JOptionPane.INFORMATION_MESSAGE);
            if (JOptionPane.showConfirmDialog(null, "Would you like to restart now?", "Restart?",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                Launcher.restartLauncher();
            }
        }

    }

    public static void voteLinks() {
        Object[] options = {"Site 1: ftbservers", "Site 2: minecraft-mp"};
        int answer = JOptionPane.showOptionDialog(null,
                "Vote links:",
                "Vote and earn Lolnet Coins!",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                null);
        if (answer == 0) {
            openURL("http://ftbservers.com/server/5456/vote");

        } else if (answer == 1) {
            openURL("http://minecraft-mp.com/server/2177/vote/");

        }

    }

    /**
     * Downloads text from a given URL and returns it as a String
     *
     * @return the text (or null if download failed)
     */
    public static String downloadTextFromUrl(String url) {
        InputStream in;
        try {
            in = new URL(url).openStream();
            Scanner scan = new Scanner(in);
            return scan.hasNext() ? scan.next() : null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
