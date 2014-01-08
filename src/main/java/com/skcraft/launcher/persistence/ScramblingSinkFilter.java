/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.persistence;

import com.google.common.io.ByteSink;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

class ScramblingSinkFilter extends ByteSink {

    private final ByteSink delegate;
    private final String key;

    public ScramblingSinkFilter(ByteSink delegate, String key) {
        this.delegate = delegate;
        this.key = key;
    }

    @Override
    public OutputStream openStream() throws IOException {
        Cipher cipher = null;
        try {
            cipher = getCipher(Cipher.ENCRYPT_MODE, key);
        } catch (Throwable e) {
            throw new IOException("Failed to create cipher", e);
        }
        return new CipherOutputStream(delegate.openStream(), cipher);
    }

    public static Cipher getCipher(int mode, String password)
            throws InvalidKeySpecException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        // These parameters were used for encrypting lastlogin on old official Minecraft launchers
        Random random = new Random(0x29482c2L);
        byte salt[] = new byte[8];
        random.nextBytes(salt);
        PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 5);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = factory.generateSecret(new PBEKeySpec(password.toCharArray()));
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        cipher.init(mode, key, paramSpec);
        return cipher;
    }

}
