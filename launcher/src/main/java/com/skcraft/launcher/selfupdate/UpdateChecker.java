/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.selfupdate;

import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.LauncherException;
import com.skcraft.launcher.util.HttpRequest;
import com.skcraft.launcher.util.SharedLocale;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.net.URL;
import java.util.concurrent.Callable;

/**
 * A worker that checks for an update to the launcher. A URL is returned
 * if there is an update to be downloaded.
 */
@Log
public class UpdateChecker implements Callable<URL> {

    private final Launcher launcher;

    public UpdateChecker(@NonNull Launcher launcher) {
        this.launcher = launcher;
    }

    @Override
    public URL call() throws Exception {
        try {
            UpdateChecker.log.info("Checking for update...");

            URL url = HttpRequest.url(launcher.getProperties().getProperty("selfUpdateUrl"));

            LatestVersionInfo versionInfo = HttpRequest.get(url)
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .asJson(LatestVersionInfo.class);

            ComparableVersion current = new ComparableVersion(launcher.getVersion());
            ComparableVersion latest = new ComparableVersion(versionInfo.getVersion());

            UpdateChecker.log.info("Latest version is " + latest + ", while current is " + current);

            if (latest.compareTo(current) >= 1) {
                UpdateChecker.log.info("Update available at " + versionInfo.getUrl());
                return versionInfo.getUrl();
            } else {
                UpdateChecker.log.info("No update required.");
                return null;
            }
        } catch (Exception e) {
            throw new LauncherException(e, SharedLocale.tr("errors.selfUpdateCheckError"));
        }
    }

}
