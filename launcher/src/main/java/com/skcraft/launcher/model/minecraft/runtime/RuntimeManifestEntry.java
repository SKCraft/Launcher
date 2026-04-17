package com.skcraft.launcher.model.minecraft.runtime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(RuntimeManifestEntry.File.class),
        @JsonSubTypes.Type(RuntimeManifestEntry.Directory.class),
        @JsonSubTypes.Type(RuntimeManifestEntry.Link.class),
})
public interface RuntimeManifestEntry {
    @Data
    @Jacksonized
    @Builder
    @JsonTypeName("file")
    @JsonIgnoreProperties(ignoreUnknown = true)
    class File implements RuntimeManifestEntry {
        private final Map<Format, DownloadInfo> downloads;
        private final boolean executable;
    }

    @NoArgsConstructor
    @JsonTypeName("directory")
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Directory implements RuntimeManifestEntry {
    }

    @Jacksonized
    @Builder
    @Data
    @JsonTypeName("link")
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Link implements RuntimeManifestEntry {
        private final String target;
    }
}
