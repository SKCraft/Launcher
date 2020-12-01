package com.skcraft.launcher.model.minecraft;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MinecraftArguments {
	@JsonProperty("game")
	private List<String> gameArguments;
}

