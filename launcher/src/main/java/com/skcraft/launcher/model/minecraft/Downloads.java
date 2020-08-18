package com.skcraft.launcher.model.minecraft;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashMap;

/**
 * @author barpec12
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Downloads {

    private Artifact artifact;
    private HashMap<String, Artifact> classifiers;
}
