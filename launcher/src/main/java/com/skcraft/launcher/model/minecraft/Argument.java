/*
 * part of the new version manifest handling, see https://github.com/SKCraft/Launcher/issues/235
 * based on the pull request https://github.com/SKCraft/Launcher/pull/265 from EazFTW
 */
package com.skcraft.launcher.model.minecraft;

import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.skcraft.launcher.util.Environment;

@JsonDeserialize(using = ArgumentDeserializer.class)
@JsonSerialize(using = ArgumentSerializer.class)
public class Argument {

    // argument can be both a string, or a complex object with a list of rules and a list of arguments 

    String[]  arguments;

    ArgumentRule[] rules; 

    public boolean applies(Environment env) {
        // some of them must be skipped
        // -Djava.library.path is already handled by com.skcraft.launcher.launch.Runner.addLibraries()
        if (argumentContains("-Djava.library.path")) {
            return false;
        }
        // width and height are already handled by com.skcraft.launcher.launch.Runner.addWindowArgs()
        if (argumentContains("--width") || argumentContains("--height")) {
            return false;
        }
        // -cp and ${classpath} are handled by com.skcraft.launcher.launch.JavaProcessBuilder
        // those are two different arguments:
        if (argumentIs("-cp") || argumentContains("${classpath}")) {
            return false;
        }
        // for all other arguments, if rules are given, check if they apply:
        if (rules != null) {
            for (ArgumentRule r : rules) {
                if (!r.applies(env))
                    return false;
            }
        }
        else if (env != null) { 
            // env if only != null for the OS specific arguments
            // i.e. those that have a rule with os != null
            // if there are no rules, it can't be an OS specific argument  
            return false; 
        }
        return true;
    }

    private boolean argumentIs(String argPart) {
        for (String arg : arguments) {
            if (arg.equalsIgnoreCase(argPart)) {
                return true;
            }
        }
        return false;
    }

    private boolean argumentContains(String argPart) {
        for (String arg : arguments) {
            if (arg.contains(argPart)) {
                return true;
            }
        }
        return false;
    }

    public void setArgument(JsonNode node) {
        if (node.isArray()) {
            arguments = new String[node.size()];
            for (int i = 0; i < node.size(); i++) {
                arguments[i] = node.get(i).asText();
            }
        }
        else { // _should_ be TextNode  
            arguments = new String[1];
            arguments[0] = node.asText();
        }
        
    }

    public void setRules(JsonNode rulesNode) throws JsonProcessingException {
        if (rulesNode.isArray()) {
            rules = new ArgumentRule[rulesNode.size()];
            for (int i = 0; i < rulesNode.size(); i++) {
                rules[i] = new ArgumentRule(rulesNode.get(i));
            }
        }
        else {
            throw new JsonGenerationException("Parsing Argument: rules is not an array.");
        }
    }

    public void addArgs(ArrayList<String> args, Environment environment) {
        if (applies(environment)) {
            for (String arg : arguments) {
                args.add(arg);
            }
        }
        
    }

}
