package com.skcraft.launcher.model.minecraft.mapper;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.List;

public class ArgumentValueSerializer extends StdSerializer<List<String>> {
	protected ArgumentValueSerializer() {
		super(TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
	}

	@Override
	public void serialize(List<String> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
		if (value.size() == 1) {
			jgen.writeString(value.get(0));
		} else {
			provider.defaultSerializeValue(value, jgen);
		}
	}
}
