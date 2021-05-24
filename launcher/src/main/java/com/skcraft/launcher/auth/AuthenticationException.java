/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.auth;

import com.skcraft.launcher.LauncherException;
import lombok.Getter;

/**
 * Thrown on authentication error.
 */
public class AuthenticationException extends LauncherException {
    @Getter
    private boolean invalidatedSession = false;

    public AuthenticationException(String message, String localizedMessage) {
        super(message, localizedMessage);
    }

    public AuthenticationException(String message) {
        super(message, message);
    }

    public AuthenticationException(String message, boolean invalidatedSession) {
        super(message, message);
        this.invalidatedSession = invalidatedSession;
    }

    public AuthenticationException(Throwable cause, String localizedMessage) {
        super(cause, localizedMessage);
    }
}
