/*
 * part of the new version manifest handling, see https://github.com/SKCraft/Launcher/issues/235
 * based on the pull request https://github.com/SKCraft/Launcher/pull/265 from EazFTW
 */
package com.skcraft.launcher.model.minecraft;

import lombok.Data;

@Data
public class VersionDownloads {

	private DownloadData client;
    private DownloadData server;

}
