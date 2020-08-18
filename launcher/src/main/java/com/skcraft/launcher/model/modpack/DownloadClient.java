package com.skcraft.launcher.model.modpack;

import lombok.Data;

@Data
public class DownloadClient {

    private String sha1;
    private int size;
    private String url;

}
