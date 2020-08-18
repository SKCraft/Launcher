package com.skcraft.launcher.model.modpack;

import lombok.Data;

@Data
public class DownloadServer {

    private String sha1;
    private int size;
    private String url;

}
