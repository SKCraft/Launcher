/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author James
 */
public class WebUtil {

    public static void main(String[] args) throws Exception {
        String url = "http://api.lolnet.co.nz:8080/modpack/public";
        System.out.println(getFiles(url));
        url = "http://api.lolnet.co.nz:8080/";
        System.out.println(getFiles(url));

    }

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
}
