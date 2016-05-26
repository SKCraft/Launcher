/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.concurrency;

public class SettableProgress implements ProgressObservable {

    private ProgressObservable delegate;
    private String status = "";
    private double progress = -1;

    public SettableProgress(String status, double progress) {
        this.status = status;
        this.progress = progress;
    }

    public SettableProgress(ProgressObservable observable) {
        this.delegate = observable;
    }

    public synchronized void observe(ProgressObservable observable) {
        delegate = observable;
    }

    public synchronized void set(String status, double progress) {
        delegate = null;
        this.progress = progress;
        this.status = status;
    }

    @Override
    public double getProgress() {
        ProgressObservable delegate = this.delegate;
        return delegate != null ? delegate.getProgress() : progress;
    }

    @Override
    public String getStatus() {
        ProgressObservable delegate = this.delegate;
        return delegate != null ? delegate.getStatus() : status;
    }

}
