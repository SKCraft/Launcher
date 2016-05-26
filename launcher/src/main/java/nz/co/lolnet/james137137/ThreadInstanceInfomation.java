/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;

import com.skcraft.launcher.Instance;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.swing.InstanceTableModel;
import static com.skcraft.launcher.swing.InstanceTableModel.exists;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author James
 */
public class ThreadInstanceInfomation implements Runnable{
    
    Instance instance;
    int rowIndex;
    public ThreadInstanceInfomation(Instance instance,int rowIndex) {
        this.instance = instance;
        this.rowIndex = rowIndex;
        HttpThreadPool.add(this);
    }

    @Override
    public void run() {
        String line = "";
        try {
                URL url;
                url = new URL(Launcher.modPackURL + "instanceicon/" + instance.getTitle().replaceAll(" ", "_") + "/info.php");
                if (!exists(url.toString())) {
                    url = null;
                }
                if (url != null) {
                    URLConnection conn = url.openConnection();
                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.flush();

                    // Get the response
                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    while ((line = rd.readLine()) != null) {
                        InstanceTableModel.instanceInfo.put(instance.getTitle(), line);
                    }
                    wr.close();
                    rd.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            line = InstanceTableModel.instanceInfo.get(instance.getTitle());
            InstanceTableModel.instanceInfo.put(instance.getTitle(), line);
            InstanceTableModel.instanceTableModel.update(false);
    }
    
}
