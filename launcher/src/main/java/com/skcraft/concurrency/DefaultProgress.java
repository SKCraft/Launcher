/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.concurrency;

import lombok.Data;

/**
 * A simple default implementation of {@link com.skcraft.concurrency.ProgressObservable}
 * with settable properties.
 */
@Data
public class DefaultProgress implements ProgressObservable {

    private String status;
    private double progress = -1;

    public DefaultProgress() {
    }

    public DefaultProgress(double progress, String status) {
        this.progress = progress;
        this.status = status;
    }
}
