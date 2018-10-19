package com.skcraft.launcher.model.modpack;


import lombok.Data;

import java.util.List;

@Data
public class LauncherJSON {

    private LatestVersions latest;
    private List<ModpackVersion> versions;

    public List<ModpackVersion> getVersions() {
        return versions;
    }

}
