/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.concurrency;

import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import java.util.concurrent.Callable;

/**
 * An extension of {@link ListenableFuture} that provides convenience methods
 * to register functions that are triggered after upon the completion of
 * the underlying task.
 *
 * <p>Dependent functions are executed using the "default" executor which
 * is specified when {@code Deferred} is first created, unless
 * the async variants are used to register the function.</p>
 *
 * @param <I> The type returned
 */
public interface Deferred<I> extends ListenableFuture<I> {

    /**
     * Returns a new Deferred that represents the asynchronous computation
     * of the given action, which is only executed upon the normal completion
     * of this Deferred in the default executor.
     *
     * @param task The task
     * @param <O> The return type of the task
     * @return The new Deferred
     */
    <O> Deferred<O> thenRun(Callable<O> task);

    /**
     * Returns a new Deferred that represents the asynchronous computation
     * of the given action, which is only executed upon the normal completion
     * of this Deferred in the provided executor.
     *
     * @param task The task
     * @param <O> The return type of the task
     * @return The new Deferred
     */
    <O> Deferred<O> thenRunAsync(Callable<O> task, ListeningExecutorService executor);

    /**
     * Returns a new Deferred that represents the asynchronous execution
     * of the given action, which is only executed upon the normal completion
     * of this Deferred in the provided executor.
     *
     * @param task The task
     * @return The new Deferred
     */
    Deferred<Void> thenRun(Runnable task);

    /**
     * Returns a new Deferred that represents the asynchronous execution
     * of the given action, which is only executed upon the normal completion
     * of this Deferred in the default executor.
     *
     * @param task The task
     * @return The new Deferred
     */
    Deferred<Void> thenRunAsync(Runnable task, ListeningExecutorService executor);

    /**
     * Returns a new Deferred that represents the asynchronous execution
     * of the given action as a side effect that does not change the value
     * passed between the prior Deferred to any dependent Deferred instances.
     *
     * <p>The given action is only executed in the default executor upon the
     * normal completion of this Deferred.</p>
     *
     * @param task The task
     * @return The new Deferred
     */
    Deferred<I> thenTap(Runnable task);

    /**
     * Returns a new Deferred that represents the asynchronous execution
     * of the given action as a side effect that does not change the value
     * passed between the prior Deferred to any dependent Deferred instances.
     *
     * <p>The given action is only executed in the provided executor upon the
     * normal completion of this Deferred.</p>
     *
     * @param task The task
     * @return The new Deferred
     */
    Deferred<I> thenTapAsync(Runnable task, ListeningExecutorService executor);

    /**
     * Returns a new Deferred that represents the asynchronous execution
     * of the given function, which transforms the value of the previous
     * Deferred into a new value.
     *
     * <p>The given action is only executed in the default executor upon the
     * normal completion of this Deferred.</p>
     *
     * @param function The function
     * @return The new Deferred
     */
    <O> Deferred<O> thenApply(Function<I, O> function);

    /**
     * Returns a new Deferred that represents the asynchronous execution
     * of the given function, which transforms the value of the previous
     * Deferred into a new value.
     *
     * <p>The given action is only executed in the provided executor upon the
     * normal completion of this Deferred.</p>
     *
     * @param function The function
     * @return The new Deferred
     */
    <O> Deferred<O> thenApplyAsync(Function<I, O> function, ListeningExecutorService executor);

    /**
     * Adds callbacks that are executed asynchronously on success or failure
     * using the default executor.
     *
     * @param onSuccess The success callback
     * @param onFailure The failure callback
     * @return The Deferred
     */
    Deferred<I> handle(Callback<I> onSuccess, Callback<Throwable> onFailure);

    /**
     * Adds callbacks that are executed asynchronously on success or failure.
     *
     * @param onSuccess The success callback
     * @param onFailure The failure callback
     * @param executor The executor
     * @return The Deferred
     */
    Deferred<I> handleAsync(Callback<I> onSuccess, Callback<Throwable> onFailure, ListeningExecutorService executor);

}
