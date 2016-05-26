/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.util;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public final class SwingExecutor extends AbstractExecutorService {

    public static final SwingExecutor INSTANCE = new SwingExecutor();

    private SwingExecutor() {
    }

    @Override
    public void execute(Runnable runnable) {
        SwingUtilities.invokeLater(runnable);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(final Callable<T> callable) {
        return new FutureTask<T>(callable) {
            @Override
            public void run() {
                try {
                    super.run();
                } catch (Throwable e) {
                    setException(e);
                }
            }
        };
    }

    @Override
    public void shutdown() {
    }

    @Override
    public List<Runnable> shutdownNow() {
        return new ArrayList<Runnable>();
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }
}