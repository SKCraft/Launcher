package com.skcraft.launcher.model.minecraft;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.skcraft.launcher.model.minecraft.mapper.MinecraftArgumentsDeserializer;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MinecraftArguments {
	@JsonProperty("game")
	@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
	@JsonDeserialize(contentUsing = MinecraftArgumentsDeserializer.class)
	private List<GameArgument> gameArguments;

	@JsonProperty("jvm")
	@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
	@JsonDeserialize(contentUsing = MinecraftArgumentsDeserializer.class)
	private List<GameArgument> jvmArguments;
}

