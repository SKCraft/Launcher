/*
 * part of the new version manifest handling, see https://github.com/SKCraft/Launcher/issues/235
 * based on the pull request https://github.com/SKCraft/Launcher/pull/265 from EazFTW
 */
package com.skcraft.launcher.model.minecraft;

import lombok.Data;

import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.skcraft.launcher.util.HttpRequest;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManifestJSON {

    private List<MinecraftVersion> versions;

    public static URL getVersionURL(URL manifestURL, String version) {
        try {
            ManifestJSON json = HttpRequest
                    .get(manifestURL)
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .asJson(ManifestJSON.class);
            for(MinecraftVersion v : json.versions) {
                if(v.getId().equalsIgnoreCase(version)) {
                    return HttpRequest.url(v.getUrl());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
