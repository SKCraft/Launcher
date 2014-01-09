/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.concurrency;

/**
 * Implementations of this interface can provide information on the progress
 * of a task.
 */
public interface ProgressObservable {

    /**
     * Get the progress value as a number between 0 and 1 (inclusive), or -1
     * if progress information is unavailable.
     *
     * @return the progress value
     */
    double getProgress();

    /**
     * Get the current status text.
     *
     * @return the status text, or null if unavailable
     */
    String getStatus();

}
