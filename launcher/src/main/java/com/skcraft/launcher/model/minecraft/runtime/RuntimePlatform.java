package com.skcraft.launcher.model.minecraft.runtime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.skcraft.launcher.util.Environment;
import com.skcraft.launcher.util.Platform;
import lombok.Getter;

public enum RuntimePlatform {
    @JsonProperty("linux") LINUX(true),
    @JsonProperty("linux-i386") LINUX_386(false),
    @JsonProperty("mac-os") MAC_OS(true),
    @JsonProperty("mac-os-arm64") MAC_OS_ARM64(true),
    @JsonProperty("windows-arm64") WINDOWS_ARM64(true),
    @JsonProperty("windows-x64") WINDOWS_X64(true),
    @JsonProperty("windows-x86") WINDOWS_X86(false);

    @Getter
    private final boolean is64Bit;

    RuntimePlatform(boolean is64Bit) {
        this.is64Bit = is64Bit;
    }

    public static RuntimePlatform from(Environment environment) {
        Platform platform = environment.getPlatform();
        String arch = environment.getArch();

        switch (platform) {
            case WINDOWS:
                if (arch.equals("aarch64")) return WINDOWS_ARM64;
                else if (arch.equals("x86")) return WINDOWS_X86;
                else return WINDOWS_X64;
            case MAC_OS_X:
                if (arch.equals("aarch64")) return MAC_OS_ARM64;
                else return MAC_OS;
            case LINUX:
                if (arch.equals("i386")) return LINUX_386;
                else return LINUX;
            default:
                return null;
        }
    }
}
