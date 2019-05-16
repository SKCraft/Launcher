/*
 * part of the new version manifest handling, see https://github.com/SKCraft/Launcher/issues/235
 * based on the pull request https://github.com/SKCraft/Launcher/pull/265 from EazFTW
 */
package com.skcraft.launcher.model.minecraft;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.skcraft.launcher.util.HttpRequest;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DownloadData {

    private String url;

    // note that naming this wrapper 'getURL' will irritate the JSON parser, resulting in url = null
    @JsonIgnore
    public URL getDownloadURL() {
        return (url == null || url.isEmpty()) ? null : HttpRequest.url(url);
    }

}