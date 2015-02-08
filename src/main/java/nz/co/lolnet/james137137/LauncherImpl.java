/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;

import com.skcraft.launcher.Launcher;
import nz.co.lolnet.lolnetlauncher.LolnetLauncher;



/**
 *
 * @author James
 */
public class LauncherImpl implements LolnetLauncher{

    @Override
    public void start() {
        Launcher.main();
    }
    
}
