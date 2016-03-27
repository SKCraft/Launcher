/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137;

import java.util.prefs.Preferences;

/**
 *
 * @author James
 */
public class LauncherGobalSettings {
    
    public static String get(String key)
    {
        return Preferences.userRoot().get(key, "");
    }
    
    public static void put(String key, String vaule)
    {
        Preferences.userRoot().put(key, vaule);
    }
}
