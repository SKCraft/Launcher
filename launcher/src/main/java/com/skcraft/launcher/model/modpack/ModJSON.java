package com.skcraft.launcher.model.modpack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModJSON {

    private LauncherMeta downloads;

}
