/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.install;

import com.google.common.io.ByteSource;
import com.google.common.io.Closer;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class ZipExtract implements Runnable {

    @Getter private final ByteSource source;
    @Getter private final File destination;
    @Getter @Setter
    private List<String> exclude;

    public ZipExtract(@NonNull ByteSource source, @NonNull File destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    public void run() {
        Closer closer = Closer.create();

        try {
            InputStream is = closer.register(source.openBufferedStream());
            ZipInputStream zis = closer.register(new ZipInputStream(is));
            ZipEntry entry;

            destination.getParentFile().mkdirs();

            while ((entry = zis.getNextEntry()) != null) {
                if (matches(entry)) {
                    File file = new File(getDestination(), entry.getName());
                    writeEntry(zis, file);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Checks if the given entry should be extracted.
     *
     * @param entry the entry
     * @return true if the entry matches the filter
     */
    private boolean matches(ZipEntry entry) {
        if (exclude != null) {
            for (String pattern : exclude) {
                if (entry.getName().startsWith(pattern)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void writeEntry(ZipInputStream zis, File path) throws IOException {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
            path.getParentFile().mkdirs();

            fos = new FileOutputStream(path);
            bos = new BufferedOutputStream(fos);
            IOUtils.copy(zis, bos);
        } finally {
            closeQuietly(bos);
            closeQuietly(fos);
        }
    }

    @Override
    public String toString() {
        return destination.getName();
    }


}
