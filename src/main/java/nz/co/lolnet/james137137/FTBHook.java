/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import org.json.XML;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author James
 */
public class FTBHook {

    public static void main(String[] args) throws Exception {
        JSONParser parser = new JSONParser();
        
        JSONObject obj;
        obj = (JSONObject) parser.parse(XML.toJSONObject(getText("http://ftb.cursecdn.com/FTB2/static/modpacks.xml")).toString());
        System.out.println(obj.toString());
        HashMap<String, HashMap<String,Object>> modpacks = (HashMap<String, HashMap<String,Object>>) obj.get("modpacks");
        List<HashMap<String,String>> FTBmodPacklist = (List<HashMap<String,String>>) modpacks.get("modpack");
        for (HashMap<String, String> FTBModpacks : FTBmodPacklist) {
            Object name = FTBModpacks.get("name").replaceAll("FTB ", "");
            Object version = FTBModpacks.get("version");
            System.out.println(name + " Version: " + version);
        }
    }
    
    public static String getText(String url) throws Exception {
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                    connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) 
            response.append(inputLine);

        in.close();

        return response.toString();
    }
}
