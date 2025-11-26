package com.skcraft.launcher.model.minecraft.runtime;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Format {
    @JsonProperty("raw") RAW,
    @JsonProperty("lzma") LZMA;
}
