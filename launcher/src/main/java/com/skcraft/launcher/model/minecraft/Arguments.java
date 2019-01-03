package com.skcraft.launcher.model.minecraft;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * @author barpec12
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Arguments {

    private List<Object> game;
    private List<Object> jvm;
}
