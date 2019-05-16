/*
 * part of the new version manifest handling, see https://github.com/SKCraft/Launcher/issues/235
 * based on the pull request https://github.com/SKCraft/Launcher/pull/265 from EazFTW
 */
package com.skcraft.launcher.model.minecraft;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;

public class ArgumentDeserializer extends StdDeserializer<Argument> {

    /**
     * serial version ID (eclipse gives a warning if it is not present)
     */
    private static final long serialVersionUID = 1L;

    public ArgumentDeserializer() {
        this(null);
    }

    public ArgumentDeserializer(Class<?> vc) { 
        super(vc); 
    }

    @Override
    public Argument deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        Argument arg = new Argument();
        JsonNode node = jp.getCodec().readTree(jp);

        if (node instanceof TextNode) {
            arg.setArgument(node);
        }
        else {
            arg.setArgument(node.get("value"));
            arg.setRules(node.get("rules"));
        }
        return arg;
    }

}
