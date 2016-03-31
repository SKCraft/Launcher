/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.statistics;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nz.co.lolnet.james137137.HttpThreadPool;

/**
 *
 * @author James
 */
public class ThreadModPackGameTime implements Runnable{
    
    String playerName;
    String modpackName;
    int timePlayed;

    public ThreadModPackGameTime(String playerName, String modpackName, int timePlayed) {
        this.playerName = playerName;
        this.modpackName = modpackName;
        this.timePlayed = timePlayed;
        HttpThreadPool.add(this);
    }

    

    @Override
    public void run() {
        try {
            LauncherStatistics.launcherGameTime(playerName,modpackName,timePlayed);
        } catch (IOException ex) {
            Logger.getLogger(ThreadLauncherIsLaunched.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
