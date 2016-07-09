/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;

import com.skcraft.launcher.Launcher;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author James
 */
public class MyLogger {

    public static boolean enabled;
    static File myLogFile = null;
    private static List<String> pendingLogs = new ArrayList<>();

    private static void setupLogger() {
        File theDir = new File(Launcher.dataDir, "debug");

        // if the directory does not exist, create it
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
        myLogFile = new File(theDir, System.currentTimeMillis() + ".txt");

        try {
            myLogFile.createNewFile();
        } catch (IOException ex) {
        }

    }

    public static void log(String string) {
        if (!enabled) {
            return;
        }
        if (myLogFile == null) {
            setupLogger();
        }
        String output;
        java.util.Date date = new java.util.Date();
        String time = new Timestamp(date.getTime()).toString();
        output = time + ":" + string;
        try {
            FileWriter fw = new FileWriter(myLogFile, true); //the true will append the new data
            fw.write(output + "\n");//appends the string to the file

            fw.close();
        } catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }
}
