package com.skcraft.launcher.model.minecraft.mapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.skcraft.launcher.model.minecraft.GameArgument;

import java.io.IOException;

public class MinecraftArgumentsDeserializer extends StdDeserializer<GameArgument> {
	protected MinecraftArgumentsDeserializer() {
		super(GameArgument.class);
	}

	@Override
	public GameArgument deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		if (!jp.hasCurrentToken()) jp.nextToken();

		if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
			return jp.readValueAs(GameArgument.class);
		} else if (jp.getCurrentToken() == JsonToken.VALUE_STRING) {
			String argument = jp.getValueAsString();
			return new GameArgument(argument);
		}

		throw new InvalidFormatException(jp, "Invalid JSON type for deserializer (not string or object)", null, GameArgument.class);
	}
}
