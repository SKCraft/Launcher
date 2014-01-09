/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.auth;

import lombok.Getter;
import lombok.NonNull;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * An offline session.
 */
public class OfflineSession implements Session {

    private static Map<String, String> dummyProperties = Collections.emptyMap();

    @Getter
    private final String name;

    /**
     * Create a new offline session using the given player name.
     *
     * @param name the player name
     */
    public OfflineSession(@NonNull String name) {
        this.name = name;
    }

    @Override
    public String getUuid() {
        return (new UUID(0, 0)).toString();
    }

    @Override
    public String getClientToken() {
        return "0";
    }

    @Override
    public String getAccessToken() {
        return "0";
    }

    @Override
    public Map<String, String> getUserProperties() {
        return dummyProperties;
    }

    @Override
    public String getSessionToken() {
        return "-";
    }

    @Override
    public UserType getUserType() {
        return UserType.LEGACY;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

}
