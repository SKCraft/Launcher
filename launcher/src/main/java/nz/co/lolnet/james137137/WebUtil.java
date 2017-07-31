/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author James
 */
public class WebUtil {

    public static void main(String[] args) throws Exception {

    }
    static boolean lolnetHTTPSYet = true;

    public static List<String> getFiles(String url) throws IOException {
        List<String> output = new ArrayList<>();
        URL oracle = new URL(url);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(oracle.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            if (inputLine.contains("HREF") && !inputLine.contains("Parent Directory")) {
                try {
                    output.add(inputLine.split("\\\">")[1].split("&nbsp;")[0]);
                } catch (Exception e) {
                }
            }
        }

        in.close();
        return output;
    }

    public static URL convertToHttpsIfPossible(URL url) {
        if (!url.getHost().toLowerCase().contains("lolnet.co.nz") || !lolnetHTTPSYet)
        {
            return url;
        }
        HttpURLConnection connection = null;

        try {
            URL myurl;
            if (!url.getProtocol().equalsIgnoreCase("https")) {
                myurl = new URL(url.toString().replaceFirst("http", "https"));
            } else {
                myurl = new URL(url.toString());
            }

            connection = (HttpURLConnection) myurl.openConnection();
            connection.setConnectTimeout(1000);
            int code = connection.getResponseCode();
            return new URL(url.toString().replaceFirst("http", "https"));
        } catch (Exception e) {
            lolnetHTTPSYet = false;
            return url;
        }
    }
}
