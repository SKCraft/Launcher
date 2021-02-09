/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.auth;

import java.util.Map;

/**
 * Represents an authenticated (or virtual) session.
 */
public interface Session {

    /**
     * Get the user's UUID.
     *
     * @return the user's UUID
     */
    String getUuid();

    /**
     * Get the user's game username.
     *
     * @return the username
     */
    String getName();

    /**
     * Get the access token.
     *
     * @return the access token
     */
    String getAccessToken();

    /**
     * Get a map of user properties.
     *
     * @return the map of user properties
     */
    Map<String, String> getUserProperties();

    /**
     * Get the session token string, which is in the form of
     * <code>token:accessToken:uuid</code> for authenticated players, and
     * simply <code>-</code> for offline players.
     *
     * @return the session token
     */
    String getSessionToken();

    /**
     * Get the user type.
     *
     * @return the user type
     */
    UserType getUserType();

    /**
     * Get the user's avatar
     *
     * @return User's avatar as a base64 string.
     */
    String getAvatarImage();

    /**
     * Return true if the user is in an online session.
     *
     * @return true if online
     */
    boolean isOnline();

    /**
     * Convert this session to a saved session
     * @return Saved session that represents this active session
     */
    default SavedSession toSavedSession() {
        SavedSession savedSession = new SavedSession();

        savedSession.setType(getUserType());
        savedSession.setUsername(getName());
        savedSession.setUuid(getUuid());
        savedSession.setAccessToken(getAccessToken());
        savedSession.setAvatarImage(getAvatarImage());

        return savedSession;
    }

}
