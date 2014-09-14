/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;

import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.selfupdate.LatestVersionInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author James
 */
public class Updater implements Runnable {

    boolean deleteMode;
    boolean updateMode;
    File fileToDelete;
    String newJarFile;
    URL url;

    public Updater(File fileToDelete, String mode) {
        if (mode.equalsIgnoreCase("delete")) {
            deleteMode = true;
            this.fileToDelete = fileToDelete;
            start();
        }
    }
    public Updater(String newJarFile) {
        updateMode = true;
        this.newJarFile = newJarFile;
        start();
    }

    private void start() {
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        if (deleteMode)
        {
            boolean delete = fileToDelete.delete();
            if (!delete)
            {
                fileToDelete.deleteOnExit();
            }
        }
        else if (updateMode)
        {
            try {
                downloadFromUrl(new URL("https://www.lolnet.co.nz/modpack/update/LolnetLauncherUpdater.jar"),"LolnetLauncherUpdater.jar");
                File updaterFile = new File(Launcher.launcherJarFile.getParentFile(), "LolnetLauncherUpdater.jar");
                Runtime rt = Runtime.getRuntime();
                
                String command = "java -jar " + 
                        "\""+updaterFile.getAbsolutePath().replaceAll("%20", " ")+"\"" + 
                        " " + 
                        "\""+newJarFile.replaceAll("%20", " ") +"\""+
                        " " 
                        +"\""+Launcher.launcherJarFile.getAbsolutePath().replaceAll("%20", " ")+"\"";
                Process pr = rt.exec(command);
                System.out.println(command);
                System.exit(0);
            } catch (IOException ex) {
                Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void downloadFromUrl(URL url, String localFilename) throws IOException {
    InputStream is = null;
    FileOutputStream fos = null;

    try {
        URLConnection urlConn = url.openConnection();//connect

        is = urlConn.getInputStream();               //get connection inputstream
        fos = new FileOutputStream(localFilename);   //open outputstream to local file

        byte[] buffer = new byte[4096];              //declare 4KB buffer
        int len;

        //while we have availble data, continue downloading and storing to local file
        while ((len = is.read(buffer)) > 0) {  
            fos.write(buffer, 0, len);
        }
    } finally {
        try {
            if (is != null) {
                is.close();
            }
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }
}
}
