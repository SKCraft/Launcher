/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.install;

import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.Launcher;

public interface InstallTask extends ProgressObservable {

    void execute(Launcher launcher) throws Exception;

}
