package com.skcraft.launcher.util;

import java.util.List;
import java.util.regex.Pattern;

// utility class for Lists
public final class ListUtil {

    public static boolean contains(List<String> list, String regex) {
        Pattern p = Pattern.compile(regex);
        
        for (String s:list) {
            if (p.matcher(s).matches()) {
                return true;
            }
        }
        return false;
    }

}
