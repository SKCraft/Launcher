/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.selfupdate;

import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.LauncherException;
import com.skcraft.launcher.util.HttpRequest;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.net.URL;
import java.util.concurrent.Callable;

import static com.skcraft.launcher.util.SharedLocale._;

@Log
public class LauncherUpdateChecker implements Callable<URL> {

    private final Launcher launcher;

    public LauncherUpdateChecker(@NonNull Launcher launcher) {
        this.launcher = launcher;
    }

    @Override
    public URL call() throws Exception {
        try {
            LauncherUpdateChecker.log.info("Checking for update...");

            URL url = HttpRequest.url(launcher.getProperties().getProperty("selfUpdateUrl"));

            LatestVersionInfo versionInfo = HttpRequest.get(url)
                    .execute()
                    .expectResponseCode(200)
                    .returnContent()
                    .asJson(LatestVersionInfo.class);

            ComparableVersion current = new ComparableVersion(launcher.getVersion());
            ComparableVersion latest = new ComparableVersion(versionInfo.getVersion());

            LauncherUpdateChecker.log.info("Latest version is " + latest + ", while current is " + current);

            if (latest.compareTo(current) >= 1) {
                LauncherUpdateChecker.log.info("Update available at " + versionInfo.getUrl());
                return versionInfo.getUrl();
            } else {
                LauncherUpdateChecker.log.info("No update required.");
                return null;
            }
        } catch (Exception e) {
            throw new LauncherException(e, _("errors.selfUpdateCheckError"));
        }
    }

}
