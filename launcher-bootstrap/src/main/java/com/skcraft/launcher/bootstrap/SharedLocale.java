/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.bootstrap;

import lombok.NonNull;
import lombok.extern.java.Log;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;

/**
 * Handles loading a shared message {@link ResourceBundle}.
 */
@Log
public class SharedLocale {

    private static Locale locale = Locale.getDefault();
    private static ResourceBundle bundle;

    /**
     * Get the current locale.
     *
     * @return the current locale
     */
    public static Locale getLocale() {
        return locale;
    }

    /**
     * Get the current resource bundle.
     *
     * @return the current resource bundle, or null if not available
     */
    public static ResourceBundle getBundle() {
        return bundle;
    }

    /**
     * Translate a string.
     *
     * <p>If the string is not available, then ${key} will be returned.</p>
     *
     * @param key the key
     * @return the translated string
     */
    public static String tr(String key) {
        if (bundle != null) {
            try {
                return bundle.getString(key);
            } catch (MissingResourceException e) {
                log.log(Level.WARNING, "Failed to find message", e);
            }
        }

        return "${" + key + "}";
    }

    /**
     * Format a translated string.
     *
     * <p>If the string is not available, then ${key}:args will be returned.</p>
     *
     * @param key the key
     * @param args arguments
     * @return a translated string
     */
    public static String tr(String key, Object... args) {
        if (bundle != null) {
            try {
                MessageFormat formatter = new MessageFormat(tr(key));
                formatter.setLocale(getLocale());
                return formatter.format(args);
            } catch (MissingResourceException e) {
                log.log(Level.WARNING, "Failed to find message", e);
            }
        }

        return "${" + key + "}:" + args;
    }

    /**
     * Load a shared resource bundle.
     *
     * @param baseName the bundle name
     * @param locale the locale
     * @return true if loaded successfully
     */
    public static boolean loadBundle(@NonNull String baseName, @NonNull Locale locale) {
        try {
            SharedLocale.locale = locale;
            bundle = ResourceBundle.getBundle(baseName, locale,
                    SharedLocale.class.getClassLoader());
            return true;
        } catch (MissingResourceException e) {
            log.log(Level.SEVERE, "Failed to load resource bundle", e);
            return false;
        }
    }
}
