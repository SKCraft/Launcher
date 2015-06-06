/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.auth;

/**
 * Represents the type of user for the account.
 */
public enum UserType {

    /**
     * Legacy accounts login with an account username.
     */
    LEGACY,
    /**
     * Mojang accounts login with an email address.
     */
    MOJANG;

    /**
     * Return a lowercase version of the enum type.
     *
     * @return the lowercase name
     */
    public String getName() {
        return name().toLowerCase();
    }


}
