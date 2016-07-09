/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;

import com.skcraft.launcher.Instance;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static nz.co.lolnet.james137137.MyLogger.log;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author James
 */
public class MemoryChecker {

    private static JSONObject memoryInfo = null;

    public static int checkMinMemory(int currentAmmount, Instance instance) {
        try {
            int suggestedAmmount = ((Long) ((JSONObject) memoryInfo.get(instance.getTitle())).get("MinMemory")).intValue();
            log("Suggested min ammout: " + suggestedAmmount);

            return suggestedAmmount > currentAmmount ? suggestedAmmount : currentAmmount;
        } catch (Exception e) {
            Logger.getLogger(MemoryChecker.class.getName()).log(Level.SEVERE, null, e);
            log("failed to get Suggested min ammout, using current: " + currentAmmount);
            return currentAmmount;
        }
    }

    public static int checkMaxMemory(int currentAmmount, Instance instance) {
        try {
            int suggestedAmmount = ((Long) ((JSONObject) memoryInfo.get(instance.getTitle())).get("MaxMemory")).intValue();
            log("Suggested max ammout: " + suggestedAmmount);
            return suggestedAmmount > currentAmmount ? suggestedAmmount : currentAmmount;
        } catch (Exception e) {
            Logger.getLogger(MemoryChecker.class.getName()).log(Level.SEVERE, null, e);
            log("failed to get Suggested max ammout, using current: " + currentAmmount);
            return currentAmmount;
        }
    }

    public static int checkpermGen(int input, Instance instance) {
        try {
            int ammout = ((Long) ((JSONObject) memoryInfo.get(instance.getTitle())).get("permGen")).intValue();
            log("Suggested permGen ammout: " + ammout);
            return ammout > input ? ammout : input;
        } catch (Exception e) {
            Logger.getLogger(MemoryChecker.class.getName()).log(Level.SEVERE, null, e);
            log("failed to get Suggested permGen ammout, using current: " + input);
            return input;
        }
    }

    public static void getMemoryInfoFromServer() throws ParseException, IOException {
        if (memoryInfo == null) {
            String jsonTxt;
            try {
                jsonTxt = downloadTextFromUrl(new URL("https://www.lolnet.co.nz/modpack/memory.json"));
            } catch (UnknownHostException e) {
                Logger.getLogger(MemoryChecker.class.getName()).log(Level.SEVERE, null, e);
                return;
            }
            
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(jsonTxt);
            memoryInfo = (JSONObject) obj;
        }
    }

    public static String downloadTextFromUrl(URL url) throws MalformedURLException, IOException , UnknownHostException{
        URLConnection con = url.openConnection();
        InputStream in = con.getInputStream();
        String encoding = con.getContentEncoding();
        encoding = encoding == null ? "UTF-8" : encoding;
        String body = IOUtils.toString(in, encoding);
        return body;
    }

}
