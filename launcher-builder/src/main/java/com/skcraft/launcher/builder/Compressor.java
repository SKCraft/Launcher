/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.builder;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Compressor {

    private static final CompressorStreamFactory factory = new CompressorStreamFactory();

    private final String extension;
    private final String format;

    public Compressor(String extension, String format) {
        this.extension = extension;
        this.format = format;
    }

    public String transformPathname(String filename) {
        return filename + "." + extension;
    }

    public InputStream createInputStream(InputStream inputStream) throws IOException {
        try {
            return factory.createCompressorInputStream(format, inputStream);
        } catch (CompressorException e) {
            throw new IOException("Failed to create decompressor", e);
        }
    }

    public OutputStream createOutputStream(OutputStream outputStream) throws IOException {
        try {
            return factory.createCompressorOutputStream(format, outputStream);
        } catch (CompressorException e) {
            throw new IOException("Failed to create compressor", e);
        }
    }

}
