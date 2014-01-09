/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.modpack;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.skcraft.launcher.install.InstallLog;
import com.skcraft.launcher.install.InstallLogFileMover;
import com.skcraft.launcher.install.Installer;
import com.skcraft.launcher.install.UpdateCache;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.skcraft.launcher.LauncherUtils.concat;

@Data
@EqualsAndHashCode(callSuper = false)
public class FileInstall extends ManifestEntry {

    private String version;
    private String hash;
    private String location;
    private String to;
    private long size;

    @JsonIgnore
    public String getImpliedVersion() {
        return checkNotNull(version != null ? version : hash);
    }

    @JsonIgnore
    public String getTargetPath() {
        return checkNotNull(this.to != null ? this.to : location);
    }

    @Override
    public void install(@NonNull Installer installer, @NonNull InstallLog log,
                        @NonNull UpdateCache cache, @NonNull File contentDir) throws MalformedURLException {
        String targetPath = getTargetPath();
        File targetFile = new File(contentDir, targetPath);
        String fileVersion = getImpliedVersion();
        URL url = concat(getManifest().getObjectsUrl(), getLocation());

        if (cache.mark(FilenameUtils.normalize(targetPath), fileVersion)) {
            long size = this.size;
            if (size <= 0) {
                size = 10 * 1024;
            }

            File tempFile = installer.getDownloader().download(url, fileVersion, size, to);
            installer.queue(new InstallLogFileMover(log, tempFile, targetFile));
        } else {
            log.add(to, to);
        }
    }

}
