/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes that are annotated with this will be saved <em>scrambled</em>
 * to disk when saved using {@link com.skcraft.launcher.persistence.Persistence}.
 * </p>
 * The data may be scrambled using an encryption algorithm, but it's not
 * done with security in mind. Decryption requires a key, and that
 * key would either have to be stored in the source code, defeating the
 * purpose of encryption, or the user would have to be prompted with a
 * password every time (possibly through an OS key ring service).
 * That creates extra hassle, so it is not done here.
 * </p>
 * Therefore , you should not depend on data that is scrambled to
 * be secure. It does, however, make it impossible for most people
 * to just read the file's contents, which is "better than nothing"
 * Documentation should not indicate to the user that the data is
 * protected however, because that would provide a false sense of
 * security.
 * </p>
 * Account data is scrambled to make it harder to extract passwords.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scrambled {

    /**
     * A key used in scrambling.
     * </p>
     * The key should not change once deployed.
     *
     * @return a key
     */
    String value();

}
