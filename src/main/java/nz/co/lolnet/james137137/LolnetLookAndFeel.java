/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;

import com.skcraft.launcher.Launcher;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author James
 */
public class LolnetLookAndFeel {

    public LolnetLookAndFeel() throws Exception {
        setupLookAndFeel();
    }

    public Properties loadParams() {
        Properties props = new Properties();
        InputStream is = null;

        // First try loading from the current directory
        try {
            File f = new File(Launcher.dataDir.getAbsoluteFile() + File.separator + "SkinLookAndFeel.properties");
            is = new FileInputStream(f);
        } catch (Exception e) {
            return null;
        }

        try {
            if (is == null) {
                // Try loading from classpath
                is = getClass().getResourceAsStream("server.properties");
            }

            // Try loading properties from the file (if found)
            props.load(is);
        } catch (Exception e) {
        }
        return props;
    }

    public void saveParamChanges(Properties props) {
        try {
            File f = new File(Launcher.dataDir.getAbsoluteFile() + File.separator + "SkinLookAndFeel.properties");
            OutputStream out = new FileOutputStream(f);
            props.store(out, "This is an optional header comment string");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupLookAndFeel() {

        Properties props = loadParams();
        System.out.println(loadParams() == null);
        if (loadParams() == null) {
            props = new Properties();

            props.put("logoString", "Lolnet");

            props.put("backgroundPattern", "off");

            props.put("windowTitleForegroundColor", "228 228 255");
            props.put("windowTitleBackgroundColor", "0 0 96");
            props.put("windowTitleColorLight", "0 0 96");
            props.put("windowTitleColorDark", "0 0 64");
            props.put("windowBorderColor", "96 96 64");

            props.put("windowInactiveTitleForegroundColor", "228 228 255");
            props.put("windowInactiveTitleBackgroundColor", "0 0 96");
            props.put("windowInactiveTitleColorLight", "0 0 96");
            props.put("windowInactiveTitleColorDark", "0 0 64");
            props.put("windowInactiveBorderColor", "32 32 128");

            props.put("menuForegroundColor", "228 228 255");
            props.put("menuBackgroundColor", "0 0 64");
            props.put("menuSelectionForegroundColor", "0 0 0");
            props.put("menuSelectionBackgroundColor", "255 192 48");
            props.put("menuColorLight", "32 32 128");
            props.put("menuColorDark", "16 16 96");

            props.put("toolbarColorLight", "32 32 128");
            props.put("toolbarColorDark", "16 16 96");

            props.put("controlForegroundColor", "228 228 255");
            props.put("controlBackgroundColor", "16 16 96");
            props.put("controlColorLight", "16 16 96");
            props.put("controlColorDark", "8 8 64");
            props.put("controlHighlightColor", "32 32 128");
            props.put("controlShadowColor", "16 16 64");
            props.put("controlDarkShadowColor", "8 8 32");

            props.put("buttonForegroundColor", "0 0 32");
            props.put("buttonBackgroundColor", "196 196 196");
            props.put("buttonColorLight", "196 196 240");
            props.put("buttonColorDark", "164 164 228");

            props.put("foregroundColor", "228 228 255");
            props.put("backgroundColor", "0 0 64");
            props.put("backgroundColorLight", "16 16 96");
            props.put("backgroundColorDark", "8 8 64");
            props.put("alterBackgroundColor", "255 0 0");
            props.put("frameColor", "96 96 64");
            props.put("gridColor", "96 96 64");
            props.put("focusCellColor", "240 0 0");

            props.put("disabledForegroundColor", "128 128 164");
            props.put("disabledBackgroundColor", "0 0 72");

            props.put("selectionForegroundColor", "0 0 0");
            props.put("selectionBackgroundColor", "196 148 16");

            props.put("inputForegroundColor", "228 228 255");
            props.put("inputBackgroundColor", "0 0 96");

            props.put("rolloverColor", "240 168 0");
            props.put("rolloverColorLight", "240 168 0");
            props.put("rolloverColorDark", "196 137 0");
            saveParamChanges(props);
        }

        // Set your theme
        com.jtattoo.plaf.noire.NoireLookAndFeel.setCurrentTheme(props);

        try {
            // select the look and feel
            UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LolnetLookAndFeel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(LolnetLookAndFeel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(LolnetLookAndFeel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(LolnetLookAndFeel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
