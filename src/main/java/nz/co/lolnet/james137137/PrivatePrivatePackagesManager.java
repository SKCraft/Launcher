/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;

import com.skcraft.launcher.Launcher;
import static com.skcraft.launcher.dialog.LauncherFrame.lolnetPingButton;
import com.skcraft.launcher.model.modpack.ManifestInfo;
import com.skcraft.launcher.model.modpack.PackageList;
import com.skcraft.launcher.util.HttpRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author James
 */
public class PrivatePrivatePackagesManager {

    private static String AllPrivatePacksCode = null;

    public static void addPrivatePackages(PackageList packages) {
        List<URL> URLs = new ArrayList<>();
        URLs.addAll(getList("private"));
        URLs.addAll(getList("public"));
        for (URL packagesURL : URLs) {
            if (exists(packagesURL.toString())) {
                try {

                    PackageList tempPackages = HttpRequest
                            .get(packagesURL)
                            .execute()
                            .expectResponseCode(200)
                            .returnContent()
                            .asJson(PackageList.class);
                    for (ManifestInfo manifestInfo : tempPackages.getPackages()) {
                        packages.packages.add(manifestInfo);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static List<String> getPublicPackagesList() {
        List<URL> URLs = getList("public");
        List<String> output = new ArrayList<String>();
        for (URL packagesURL : URLs) {
            try {

                PackageList tempPackages = HttpRequest
                        .get(packagesURL)
                        .execute()
                        .expectResponseCode(200)
                        .returnContent()
                        .asJson(PackageList.class);
                for (ManifestInfo manifestInfo : tempPackages.getPackages()) {
                    output.add(manifestInfo.getName());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return output;
    }

    public static List<String> getPrivateList() {
        List<URL> URLs = getList("private");
        List<String> output = new ArrayList<String>();
        for (URL packagesURL : URLs) {
            try {

                PackageList tempPackages = HttpRequest
                        .get(packagesURL)
                        .execute()
                        .expectResponseCode(200)
                        .returnContent()
                        .asJson(PackageList.class);
                for (ManifestInfo manifestInfo : tempPackages.getPackages()) {
                    output.add(manifestInfo.getName());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return output;
    }

    public static List<String> getAllList() {
        List<URL> URLs = getList("all");
        List<String> output = new ArrayList<String>();
        for (URL packagesURL : URLs) {
            try {

                PackageList tempPackages = HttpRequest
                        .get(packagesURL)
                        .execute()
                        .expectResponseCode(200)
                        .returnContent()
                        .asJson(PackageList.class);
                for (ManifestInfo manifestInfo : tempPackages.getPackages()) {
                    output.add(manifestInfo.getName());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return output;
    }

    private static List<URL> getList(String mode) {

        List<URL> packagesURL = new ArrayList<URL>();
        if (mode.equals("public") || mode.equals("all")) {
            List<String> publicList = getPublicList();
            System.out.println(publicList.size());
            for (String code : publicList) {
                try {
                    packagesURL.add(Launcher.checkURL(new URL(Launcher.modPackURL + "public/" + code + "?key=%s")));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        if (mode.equals("private") || mode.equals("all")) {
            List<String> privateCodeList = getCodes();
            if (AllPrivatePacksCode != null) {
                privateCodeList = getPrivateList(AllPrivatePacksCode);
            }
            for (String code : privateCodeList) {
                try {
                    packagesURL.add(Launcher.checkURL(new URL(Launcher.modPackURL + "private/" + code + ".json" + "?key=%s")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            List<String> publicPrivateList = getPublicPrivateList();
            for (String code : publicPrivateList) {
                try {
                    packagesURL.add(Launcher.checkURL(new URL(Launcher.modPackURL + "private/" + code + ".json" + "?key=%s")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return packagesURL;
    }

    private static List<String> getCodes() {
        List<String> codeList = new ArrayList<String>();
        File codeFile = new File(Launcher.dataDir, "codes.txt");
        boolean addedIWantToGoPlaces = false;
        boolean addedIDontOwnMicrosoft = false;
        if (codeFile.exists()) {
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(codeFile));

                for (String line; (line = br.readLine()) != null;) {
                    if (line.startsWith("lolnet:")) {
                        codeList.add(line.split(":")[1]);
                    } else if (line.startsWith("launcher:")) {
                        switch (line.split(":")[1]) {
                            case "showmethemoney":
                                lolnetPingButton.setVisible(true);
                                break;
                            case "IWantToGoPlaces":
                                LauncherGobalSettings.put("IWantToGoPlaces", "true");
                                addedIWantToGoPlaces = true;
                                break;
                            case "IDontOwnMicrosoft":
                                LauncherGobalSettings.put("IDontOwnMicrosoft", "true");
                                addedIDontOwnMicrosoft = true;
                                break;
                        }
                    } else if (line.startsWith("EveryPrivatePack:")) {
                        String[] split1 = line.split(":");
                        if (split1.length == 2) {
                            String code = split1[1];
                            PrivatePrivatePackagesManager.AllPrivatePacksCode = code;
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PrivatePrivatePackagesManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PrivatePrivatePackagesManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (!addedIWantToGoPlaces) {
            LauncherGobalSettings.put("IWantToGoPlaces", "false");
        }

        if (!addedIDontOwnMicrosoft) {
            LauncherGobalSettings.put("IDontOwnMicrosoft", "false");
        }

        return codeList;
    }

    private static List<String> getPrivateList(String key) {
        List<String> publicList = new ArrayList<String>();
        try {
            URL url = new URL(Launcher.modPackURL + "" + key + ".php");
            url = Launcher.checkURL(url);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;
            while ((line = rd.readLine()) != null) {
                String[] split = line.split("~~");
                for (String string : split) {
                    if (string.length() >= 2) {
                        publicList.add(string.replaceAll(".json", ""));
                    }
                }
            }
            wr.close();
            rd.close();
        } catch (Exception e) {
            return publicList;
        }

        return publicList;
    }

    private static List<String> getPublicList() {
        List<String> publicList = new ArrayList<String>();
        try {
            URL url = new URL(Launcher.modPackURL + "listpackages.php");
            System.out.println(url.toString());
            url = Launcher.checkURL(url);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;
            while ((line = rd.readLine()) != null) {
                System.out.println(line);
                String[] split = line.split("~~");
                for (String string : split) {
                    if (string.length() >= 2) {
                        publicList.add(string);
                    }
                }
            }
            wr.close();
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
            return publicList;
        }

        return publicList;
    }

    private static List<String> getPublicPrivateList() {
        List<String> publicList = new ArrayList<String>();
        try {
            URL url = new URL(Launcher.modPackURL + "listpublicprivatepackages.php");
            Launcher.checkURL(url);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;
            while ((line = rd.readLine()) != null) {
                String[] split = line.split("~~");
                for (String string : split) {
                    if (string.length() >= 2) {
                        publicList.add(string);
                    }
                }
            }
            wr.close();
            rd.close();
        } catch (Exception e) {
            return publicList;
        }

        return publicList;
    }

    public static boolean exists(String URLName) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con
                    = (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
