package com.skcraft.launcher.model.minecraft.runtime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Data
@Jacksonized
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuntimeManifest {
    private final Map<String, RuntimeManifestEntry> files;
}
