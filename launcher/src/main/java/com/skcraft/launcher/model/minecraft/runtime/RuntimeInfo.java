package com.skcraft.launcher.model.minecraft.runtime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuntimeInfo {
    private final DownloadInfo manifest;
    private final Version version;

    @Data
    @Jacksonized
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Version {
        private final String name;
        private final String released;
    }
}
