package com.skcraft.launcher.model.minecraft.runtime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.skcraft.launcher.model.minecraft.JavaVersion;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RuntimeList {
    @JsonValue
    private final Map<RuntimePlatform, Map<String, List<RuntimeInfo>>> runtimesByPlatform;

    @JsonCreator
    public RuntimeList(Map<RuntimePlatform, Map<String, List<RuntimeInfo>>> runtimesByPlatform) {
        this.runtimesByPlatform = runtimesByPlatform;
    }

    public RuntimeInfo getRuntime(RuntimePlatform platform, JavaVersion target) {
        if (platform == null) return null;
        return runtimesByPlatform.get(platform).get(target.getComponent()).get(0);
    }
}
