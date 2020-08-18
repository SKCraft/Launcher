package com.skcraft.launcher.model.minecraft;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @author barpec12
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Artifact {

    private String path;
    private String url;
}
