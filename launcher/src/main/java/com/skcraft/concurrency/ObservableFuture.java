/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.concurrency;

import com.google.common.util.concurrent.ListenableFuture;
import lombok.NonNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A pair of ProgressObservable and ListenableFuture.
 *
 * @param <V> the result type
 */
public class ObservableFuture<V> implements ListenableFuture<V>, ProgressObservable {

    private final ListenableFuture<V> future;
    private final ProgressObservable observable;

    /**
     * Construct a new ObservableFuture.
     *
     * @param future the delegate future
     * @param observable the observable
     */
    public ObservableFuture(@NonNull ListenableFuture<V> future, @NonNull ProgressObservable observable) {
        this.future = future;
        this.observable = observable;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public void addListener(Runnable listener, Executor executor) {
        future.addListener(listener, executor);
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

    @Override
    public String toString() {
        return observable.toString();
    }

    @Override
    public double getProgress() {
        return observable.getProgress();
    }

    @Override
    public String getStatus() {
        return observable.getStatus();
    }

}
