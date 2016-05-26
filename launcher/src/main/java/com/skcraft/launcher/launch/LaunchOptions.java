/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.launch;

import com.skcraft.launcher.Instance;
import com.skcraft.launcher.auth.Session;
import lombok.Data;
import lombok.Getter;

import java.awt.*;

import static com.google.common.base.Preconditions.checkNotNull;

@Data
public class LaunchOptions {

    private final Window window;
    private final Instance instance;
    private final UpdatePolicy updatePolicy;
    private final LaunchListener listener;
    private final Session session;

    @Data
    public static class Builder {

        private Window window = null;
        private Instance instance;
        private UpdatePolicy updatePolicy = UpdatePolicy.UPDATE_IF_SESSION_ONLINE;
        private LaunchListener listener = new DummyLaunchListener();
        private Session session;

        public Builder setWindow(Window window) {
            this.window = window;
            return this;
        }

        public Builder setInstance(Instance instance) {
            this.instance = instance;
            return this;
        }

        public Builder setUpdatePolicy(UpdatePolicy updatePolicy) {
            checkNotNull(updatePolicy, "updatePolicy");
            this.updatePolicy = updatePolicy;
            return this;
        }

        public Builder setListener(LaunchListener listener) {
            checkNotNull(listener, "listener");
            this.listener = listener;
            return this;
        }

        public Builder setSession(Session session) {
            this.session = session;
            return this;
        }

        public LaunchOptions build() {
            checkNotNull(instance, "instance");
            return new LaunchOptions(window, instance, updatePolicy, listener, session);
        }
    }

    public enum UpdatePolicy {
        NO_UPDATE(false),
        UPDATE_IF_SESSION_ONLINE(true),
        ALWAYS_UPDATE(true);

        @Getter
        private final boolean updateEnabled;

        UpdatePolicy(boolean updateEnabled) {
            this.updateEnabled = updateEnabled;
        }
    }

    private static class DummyLaunchListener implements LaunchListener {
        @Override
        public void instancesUpdated() {
        }

        @Override
        public void gameStarted() {
        }

        @Override
        public void gameClosed() {
        }
    }

}
