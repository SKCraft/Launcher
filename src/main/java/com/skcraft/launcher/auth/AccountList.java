/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.auth;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.skcraft.launcher.persistence.Scrambled;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A list of accounts that can be stored to disk.
 */
@Scrambled("ACCOUNT_LIST")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.NONE)
public class AccountList extends AbstractListModel implements ComboBoxModel {

    @JsonProperty
    @Getter
    private List<Account> accounts = new ArrayList<Account>();
    private transient Account selected;

    /**
     * Add a new account.
     *
     * <p>If there is already an existing account with the same ID, then the
     * new account will not be added.</p>
     *
     * @param account the account to add
     */
    public synchronized void add(@NonNull Account account) {
        if (!accounts.contains(account)) {
            accounts.add(account);
            Collections.sort(accounts);
            fireContentsChanged(this, 0, accounts.size());
        }
    }

    /**
     * Remove an account.
     *
     * @param account the account
     */
    public synchronized void remove(@NonNull Account account) {
        Iterator<Account> it = accounts.iterator();
        while (it.hasNext()) {
            Account other = it.next();
            if (other.equals(account)) {
                it.remove();
                fireContentsChanged(this, 0, accounts.size() + 1);
                break;
            }
        }
    }

    /**
     * Set the list of accounts.
     *
     * @param accounts the list of accounts
     */
    public synchronized void setAccounts(@NonNull List<Account> accounts) {
        this.accounts = accounts;
        Collections.sort(accounts);
    }

    @Override
    @JsonIgnore
    public synchronized int getSize() {
        return accounts.size();
    }

    @Override
    public synchronized Account getElementAt(int index) {
        try {
            return accounts.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public void setSelectedItem(Object item) {
        if (item == null) {
            selected = null;
            return;
        }

        if (item instanceof Account) {
            this.selected = (Account) item;
        } else {
            String id = String.valueOf(item).trim();
            Account account = new Account(id);
            for (Account test : accounts) {
                if (test.equals(account)) {
                    account = test;
                    break;
                }
            }
            selected = account;
        }

        if (selected.getId() == null || selected.getId().isEmpty()) {
            selected = null;
        }
    }

    @Override
    @JsonIgnore
    public Account getSelectedItem() {
        return selected;
    }

    public synchronized void forgetPasswords() {
        for (Account account : accounts) {
            account.setPassword(null);
        }
    }
}
