/*
 * part of the new version manifest handling, see https://github.com/SKCraft/Launcher/issues/235
 * based on the pull request https://github.com/SKCraft/Launcher/pull/265 from EazFTW
 */
package com.skcraft.launcher.model.minecraft;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ArgumentSerializer extends StdSerializer<Argument> {

    public ArgumentSerializer() {
        this(null);
    }

    public ArgumentSerializer(Class<Argument> t) {
        super(t);
    }

    @Override
    public void serialize(Argument value, JsonGenerator jgen, SerializerProvider provider) 
      throws IOException, JsonProcessingException {
        if (value.arguments.length == 1 && value.rules == null) {
            jgen.writeString(value.arguments[0]);
        }
        else {
            jgen.writeStartObject();
            jgen.writeArrayFieldStart("rules");
            for (ArgumentRule rule : value.rules) {
                // delegate to Rule, so member can stay private:
                rule.serialize(jgen);
            }
            jgen.writeEndArray();
            if (value.arguments.length == 1) {
                jgen.writeStringField("value", value.arguments[0]);
            }
            else {
                jgen.writeArrayFieldStart("value");
                for (String arg : value.arguments) {
                    jgen.writeString(arg);
                }                
                jgen.writeEndArray();
            }
            jgen.writeEndObject();
        }
    }
}
