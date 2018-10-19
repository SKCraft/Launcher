package com.skcraft.launcher.model.modpack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModpackVersion {

    private String id;
    private String type;
    private String url;
    private String time;
    private String releaseTime;

    public String getURL() {
        return url;
    }

    public String getID() {
        return id;
    }

    public String getTime() {
        return time;
    }

    public String getType() {
        return type;
    }

    public String getReleaseTime() {
        return releaseTime;
    }

}
