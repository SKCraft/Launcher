/*
 * part of the new version manifest handling, see https://github.com/SKCraft/Launcher/issues/235
 * based on the pull request https://github.com/SKCraft/Launcher/pull/265 from EazFTW
 */
package com.skcraft.launcher.model.minecraft;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.skcraft.launcher.util.Environment;

import lombok.extern.java.Log;

@Log
public class ArgumentRule {

    private String action;
    private Map<String, Boolean> features;
    private Map<String, String> os;

    public ArgumentRule(JsonNode node) throws JsonProcessingException {
        action = node.get("action").asText();
        if (node.has("features")) { 
            features = new HashMap<String, Boolean>();
            JsonNode featNode = node.get("features");
            if (featNode.isObject()) {
                for (Iterator<String> feats  = featNode.fieldNames(); feats.hasNext(); ) {
                    String feat = feats.next();
                    features.put(feat, featNode.get(feat).asBoolean());
                }
            }
            else {
                throw new JsonGenerationException("Parsing Argument: rules.features is not an object.");
            }
        }
        if (node.has("os")) { 
            os = new HashMap<String, String>();
            JsonNode osNode = node.get("os");
            if (osNode.isObject()) {
                for (Iterator<String> osSpec  = osNode.fieldNames(); osSpec.hasNext(); ) {
                    String spec = osSpec.next();
                    os.put(spec, osNode.get(spec).asText());
                }
            }
            else {
                throw new JsonGenerationException("Parsing Argument: rules.features is not an object.");
            }
        }
    }

    public boolean applies(Environment env) {
        boolean conditionsMet = appliesInternal(env);
        // this assumes that any action except "allow" means "deny" 
        // TODO: is this true? I have only seen "allow"
        return action.equalsIgnoreCase("allow") ? conditionsMet : !conditionsMet;
    }

    private boolean appliesInternal(Environment env) {
        // OS-dependent conditions:
        if (env != null) {
            // must be an OS-dependent rule:
            if (os == null)
                return false;
            for (Map.Entry<String, String> entry : os.entrySet()) {
                if (!envConditionMet(entry.getKey(), entry.getValue(), env))
                    return false;
            }
        }
        else {
            // must not be an os-independent rule:
            if (os != null)
                return false;
        }

        // OS-independent conditions: as of now, I haven't seen that
        // both are present, but to be safe, check both even if env is set
        if (features != null) {
            for (Map.Entry<String, Boolean> entry : features.entrySet()) {
                if (!conditionMet(entry.getKey(), entry.getValue()))
                    return false;
            }
        }
        // all conditions met
        return true;
    }

    private boolean conditionMet(String key, Boolean value) {
        // the only ones seen so far are is_demo_user and has_custom_resolution
        if (key.equalsIgnoreCase("is_demo_user")) {
            // TODO: how to find out if it is a demo user? Is this even possible?
            return false;
        }
        if (key.equalsIgnoreCase("has_custom_resolution")) {
            // as of now only used to set --width and --height
            // those are already handled in Runner.addWindowArgs()
            // which uses values from the config, and I don't know what else to do here, so
            // simply ignore it
            return false;
        }
        // unknown key: TODO: implement it!
        log.warning("[ArgumentRule] condition '" + key + "' not yet implemented!");
        
        return false;
    }

    private boolean envConditionMet(String key, String value, Environment env) {
        if (key.equalsIgnoreCase("name")) {
            // only "windows" and "osx" was seen in the wild ...
            switch (env.getPlatform()) {
            case WINDOWS:
                return value.equalsIgnoreCase("windows");
            case MAC_OS_X:
                return value.equalsIgnoreCase("osx");
            case LINUX:
                // we can only assume
                return value.toLowerCase().contains("linux");
            case SOLARIS:
                // we can only assume
                return value.toLowerCase().contains("solaris");
            case UNKNOWN:
                // no way to match this
                return false;
            }
        }
        if (key.equalsIgnoreCase("version")) {
            // value is a regex
            return Pattern.matches(value, env.getPlatformVersion());
        }
        if (key.equalsIgnoreCase("arch")) { // introduced after 1.13.2
            // only "x86"  was seen. the other is probably x64? 
            if (value.equalsIgnoreCase("x86")) {
                return env.getArchBits().equals("32");
            }
            // can we simply assume that x64 is the only other possible value?
            // if it is a value at all ... 
            if (value.contains("64")) { // this is a bit more generic
                return env.getArchBits().equals("64");
            }
            // (as of now), environment returns either 32 or 64, so every other value 
            // should either be added to one of the conditions above, or can't be matched:
            log.warning("[ArgumentRule] OS condition '" + key + "': unknown architecture " + value);
            return false;
        }
        // unknown key: TODO: implement it!
        log.warning("[ArgumentRule] OS condition '" + key + "' not yet implemented!");

        return false;
    }

    public void serialize(JsonGenerator jgen) throws JsonProcessingException, IOException {
        jgen.writeStartObject();
        jgen.writeStringField("action", action);
        if (features != null) {
            jgen.writeObjectFieldStart("features");
            for (Map.Entry<String, Boolean> entry : features.entrySet()) {
                jgen.writeBooleanField(entry.getKey(), entry.getValue());
            }
            jgen.writeEndObject();
        }
        if (os != null) {
            jgen.writeObjectFieldStart("os");
            for (Map.Entry<String, String> entry : os.entrySet()) {
                jgen.writeStringField(entry.getKey(), entry.getValue());
            }
            jgen.writeEndObject();
        }
        jgen.writeEndObject();
    }

}
