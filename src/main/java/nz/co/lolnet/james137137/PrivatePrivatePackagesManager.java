/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;

import com.skcraft.launcher.model.modpack.ManifestInfo;
import com.skcraft.launcher.model.modpack.PackageList;
import com.skcraft.launcher.util.HttpRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author James
 */
public class PrivatePrivatePackagesManager {

    public static File dir;

    public static void addPrivatePackages(PackageList packages) {

        try {
            List<URL> URLs = getList();
            for (URL packagesURL : URLs) {
                PackageList tempPackages = HttpRequest
                        .get(packagesURL)
                        .execute()
                        .expectResponseCode(200)
                        .returnContent()
                        .asJson(PackageList.class);
                for (ManifestInfo manifestInfo : tempPackages.getPackages()) {
                    packages.packages.add(manifestInfo);
                }
            }

        } catch (Exception e) {
        }

    }

    private static List<URL> getList() throws MalformedURLException {
        List<String> codeList = getCodes();
        List<URL> packagesURL = new ArrayList<URL>();
        for (String code : codeList) {
            packagesURL.add(new URL("https://www.lolnet.co.nz/modpack/private/" + code + ".json" + "?key=%s"));
        }
        return packagesURL;
    }

    private static List<String> getCodes() {
        List<String> codeList = new ArrayList<String>();
        File codeFile = new File(dir, "codes.txt");
        if (codeFile.exists()) {
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(codeFile));
                for (String line; (line = br.readLine()) != null;) {
                    if (line.startsWith("lolnet:"))
                    {
                        codeList.add(line.split(":")[1]);
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PrivatePrivatePackagesManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PrivatePrivatePackagesManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return codeList;
    }

    public static void setDirectory(File dirinput) {
        dir = dirinput;
    }

}
