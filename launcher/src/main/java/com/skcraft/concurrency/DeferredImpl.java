/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.concurrency;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import java.util.concurrent.*;

class DeferredImpl<I> implements Deferred<I> {

    private final ListenableFuture<I> future;
    private final ListeningExecutorService defaultExecutor;

    DeferredImpl(ListenableFuture<I> future, ListeningExecutorService defaultExecutor) {
        this.future = future;
        this.defaultExecutor = defaultExecutor;
    }

    @Override
    public <O> Deferred<O> thenRun(Callable<O> task) {
        return thenRunAsync(task, defaultExecutor);
    }

    @Override
    public <O> Deferred<O> thenRunAsync(final Callable<O> task, ListeningExecutorService executor) {
        return new DeferredImpl<O>(Futures.transform(future, new Function<I, O>() {
            @Override
            public O apply(I input) {
                try {
                    return task.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, executor), defaultExecutor);
    }

    @Override
    public Deferred<Void> thenRun(Runnable task) {
        return thenRunAsync(task, defaultExecutor);
    }

    @Override
    public Deferred<Void> thenRunAsync(final Runnable task, ListeningExecutorService executor) {
        return new DeferredImpl<Void>(Futures.transform(future, new Function<I, Void>() {
            @Override
            public Void apply(I input) {
                task.run();
                return null;
            }
        }), defaultExecutor);
    }

    @Override
    public Deferred<I> thenTap(Runnable task) {
        return thenTapAsync(task, defaultExecutor);
    }

    @Override
    public Deferred<I> thenTapAsync(final Runnable task, ListeningExecutorService executor) {
        return thenApplyAsync(new Function<I, I>() {
            @Override
            public I apply(I input) {
                task.run();
                return input;
            }
        }, executor);
    }

    @Override
    public <O> Deferred<O> thenApply(Function<I, O> function) {
        return thenApplyAsync(function, defaultExecutor);
    }

    @Override
    public <O> Deferred<O> thenApplyAsync(Function<I, O> function, ListeningExecutorService executor) {
        return new DeferredImpl<O>(Futures.transform(future, function, executor), defaultExecutor);
    }

    @Override
    public Deferred<I> handle(Callback<I> onSuccess, Callback<Throwable> onFailure) {
        return handleAsync(onSuccess, onFailure, defaultExecutor);
    }

    @Override
    public Deferred<I> handleAsync(final Callback<I> onSuccess, final Callback<Throwable> onFailure, ListeningExecutorService executor) {
        Futures.addCallback(future, new FutureCallback<I>() {
            @Override
            public void onSuccess(I result) {
                onSuccess.handle(result);
            }

            @Override
            public void onFailure(Throwable t) {
                onFailure.handle(t);
            }
        }, executor);

        return this;
    }

    @Override
    public void addListener(Runnable listener, Executor executor) {
        future.addListener(listener, executor);
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
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public I get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public I get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

}
