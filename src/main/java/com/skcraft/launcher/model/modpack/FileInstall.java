/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.modpack;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.skcraft.launcher.update.FileDistribute;
import com.skcraft.launcher.update.UpdateCache;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.skcraft.launcher.LauncherUtils.concat;

@Data
@EqualsAndHashCode(callSuper = false)
public class FileInstall extends Task {

    private String version;
    private String hash;
    private String location;
    private String to;

    @JsonIgnore
    public String getImpliedVersion() {
        return checkNotNull(version != null ? version : hash);
    }

    @JsonIgnore
    public String getTargetPath() {
        return checkNotNull(this.to != null ? this.to : location);
    }

    @Override
    public void run() {
        UpdateCache updateCache = getInstaller().getUpdateCache();
        String targetPath = getTargetPath();
        URL url;

        try {
            url = concat(getManifest().getObjectsURL(), getLocation());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid URL encountered", e);
        }

        try {
            if (updateCache.mark(FilenameUtils.normalize(targetPath), getImpliedVersion())) {
                File targetFile = new File(getInstaller().getDestinationDir(), targetPath);
                File sourceFile = getInstaller().download(url, getImpliedVersion());
                List<File> targets = new ArrayList<File>();
                targets.add(targetFile);
                getInstaller().submit(new FileDistribute(sourceFile, targets));
            }

            getInstaller().getCurrentLog().add(to, to);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new RuntimeException("Failed to download " + url.toString(), e);
        }
    }

}
