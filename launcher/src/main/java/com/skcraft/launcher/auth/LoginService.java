/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.auth;

import java.io.IOException;

/**
 * A service for creating authenticated sessions.
 */
public interface LoginService {

    /**
     * Attempt to restore a saved session into an active session.
     *
     * @param savedSession Session to restore
     * @return An authenticated session, which corresponds to a Minecraft account
     * @throws IOException thrown on I/O error
     * @throws InterruptedException thrown if interrupted
     * @throws AuthenticationException thrown on an authentication error
     */
    Session restore(SavedSession savedSession)
            throws IOException, InterruptedException, AuthenticationException;

}
