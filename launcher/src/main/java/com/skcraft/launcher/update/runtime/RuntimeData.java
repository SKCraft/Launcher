package com.skcraft.launcher.update.runtime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuntimeData {
    private final String component;
    private final String version;
    @JsonProperty("64Bit")
    private final boolean is64Bit;
}
