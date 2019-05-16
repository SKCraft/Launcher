/*
 * part of the new version manifest handling, see https://github.com/SKCraft/Launcher/issues/235
 * based on the pull request https://github.com/SKCraft/Launcher/pull/265 from EazFTW
 */
package com.skcraft.launcher.model.minecraft;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.skcraft.launcher.util.Environment;

import lombok.Data;

@Data
public class Arguments {

    private Argument[] game;
    private Argument[] jvm;

    @JsonIgnore
    public String[] getGameArguments() {
        ArrayList<String> args = new ArrayList<String>();
        
        for (Argument arg : game) {
            arg.addArgs(args, null);
        }
        
        return args.toArray(new String[args.size()]);
    }

    @JsonIgnore
    public String[] getJVMArgs() {
        if (jvm != null) {
            ArrayList<String> args = new ArrayList<String>(); 
            
            for (Argument arg : jvm) {
                arg.addArgs(args, null);
            }
            return args.toArray(new String[args.size()]);
        }
        return null;
    }

    @JsonIgnore
    public List<String> getOSDependentArgs(Environment environment) {
        if (jvm != null) {
            ArrayList<String> args = new ArrayList<String>(); 
            
            for (Argument arg : jvm) {
                arg.addArgs(args, environment);
            }
            return args;
        }
        return null;
    }
}
