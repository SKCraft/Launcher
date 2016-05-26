/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

/**
 * A human-readable error wrapper.
 */
public class LauncherException extends Exception {

    private final String localizedMessage;

    public LauncherException(String message, String localizedMessage) {
        super(message);
        this.localizedMessage = localizedMessage;
    }

    public LauncherException(Throwable cause, String localizedMessage) {
        super(cause.getMessage(), cause);
        this.localizedMessage = localizedMessage;
    }

    @Override
    public String getLocalizedMessage() {
        return localizedMessage;
    }
}
