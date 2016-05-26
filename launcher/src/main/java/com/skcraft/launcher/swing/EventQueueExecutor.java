/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.swing;

import java.awt.*;
import java.util.concurrent.Executor;

public class EventQueueExecutor implements Executor {

    public static final EventQueueExecutor INSTANCE = new EventQueueExecutor();

    private EventQueueExecutor() {
    }

    @Override
    public void execute(Runnable runnable) {
        EventQueue.invokeLater(runnable);
    }

}
