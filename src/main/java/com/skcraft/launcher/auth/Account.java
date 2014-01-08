/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;

/**
 * A user account that can be stored and loaded.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account implements Comparable<Account> {

    private String id;
    private String password;
    private Date lastUsed;

    /**
     * Create a new account.
     */
    public Account() {
    }

    /**
     * Create a new account with the given ID.
     *
     * @param id the ID
     */
    public Account(String id) {
        setId(id);
    }

    /**
     * Set the account's stored password, that may be stored to disk.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        if (password != null && password.isEmpty()) {
            password = null;
        }
        this.password = Strings.emptyToNull(password);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        if (!id.equalsIgnoreCase(account.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.toLowerCase().hashCode();
    }

    @Override
    public int compareTo(@NonNull Account o) {
        Date otherDate = o.getLastUsed();

        if (otherDate == null && lastUsed == null) {
            return 0;
        } else if (otherDate == null) {
            return -1;
        } else if (lastUsed == null) {
            return 1;
        } else {
            return -lastUsed.compareTo(otherDate);
        }
    }

    @Override
    public String toString() {
        return getId();
    }

}
