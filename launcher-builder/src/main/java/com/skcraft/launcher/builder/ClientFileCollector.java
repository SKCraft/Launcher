/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.builder;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.skcraft.launcher.model.modpack.FileInstall;
import com.skcraft.launcher.model.modpack.Manifest;
import com.skcraft.launcher.util.HttpRequest;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Walks a path and adds hashed path versions to the given
 * {@link com.skcraft.launcher.model.modpack.Manifest}.
 */
@Log
public class ClientFileCollector extends DirectoryWalker {

    public static final String URL_FILE_SUFFIX = ".url.txt";

    private final Manifest manifest;
    private final PropertiesApplicator applicator;
    private final File destDir;
    private HashFunction hf = Hashing.sha1();

    /**
     * Create a new collector.
     *
     * @param manifest the manifest
     * @param applicator applies properties to manifest entries
     * @param destDir the destination directory to copy the hashed objects
     */
    public ClientFileCollector(@NonNull Manifest manifest, @NonNull PropertiesApplicator applicator,
                               @NonNull File destDir) {
        this.manifest = manifest;
        this.applicator = applicator;
        this.destDir = destDir;
    }

    @Override
    protected DirectoryBehavior getBehavior(@NonNull String name) {
        return getDirectoryBehavior(name);
    }

    @Override
    protected void onFile(File file, String relPath) throws IOException {
        ClientFileCollector.log.info(String.format("Get %s from %s...", relPath, file.getAbsolutePath()));
        if (
                (file.getName().endsWith(FileInfoScanner.FILE_SUFFIX) || file.getName().endsWith(URL_FILE_SUFFIX))
                && new File(file.getAbsoluteFile().getParentFile(), file.getName().replace(URL_FILE_SUFFIX, "")).exists()
        ) {
            ClientFileCollector.log.info(String.format("Get %s ignored ...", relPath));
            return;
        }

        File urlFile;
        if(file.getName().endsWith(URL_FILE_SUFFIX))
        {
            urlFile = file;
            file    = new File(file.getAbsoluteFile().getParentFile(), file.getName().replace(URL_FILE_SUFFIX, ""));
            relPath = relPath.replace(URL_FILE_SUFFIX, "");
        }
        else
        {
            urlFile = new File(file.getAbsoluteFile().getParentFile(), file.getName() + URL_FILE_SUFFIX);
        }
        
        // url.txt override file
        String location, hash;
        boolean copy = true;
        if (urlFile.exists() &&
                (
                        !file.exists() || !System.getProperty("com.skcraft.builder.ignoreURLOverrides", "false")
                                                 .equalsIgnoreCase("true")
                )
        ) {
            location = Files.readFirstLine(urlFile, Charset.defaultCharset());
            copy = false;

            if (file.exists())
            {
                hash = Files.hash(file, hf).toString();
            }
            else
            {
                ClientFileCollector.log.info(String.format("Download %s from %s...", relPath, location));
                File tempFile = File.createTempFile("com.skcraft.builder", null);
                URL url = HttpRequest.url(location.trim());
                try {
                    HttpRequest.get(url)
                            .execute()
                            .expectResponseCode(200)
                            .saveContent(tempFile);
                    hash = Files.hash(tempFile, hf).toString();
                } catch (InterruptedException e) {
                    ClientFileCollector.log.warning(String.format("Download from %s failed! ", location));
                    throw new IOException(e);
                } finally {
                    if(!tempFile.delete()) {
                        ClientFileCollector.log.warning(String.format("Unable to delete %s! ", tempFile.getAbsolutePath()));
                    }
                }
            }
        } else {
            hash = Files.hash(file, hf).toString();
            location = hash.substring(0, 2) + "/" + hash.substring(2, 4) + "/" + hash;
        }

        FileInstall entry = new FileInstall();
        String to = FilenameUtils.separatorsToUnix(FilenameUtils.normalize(relPath));
        
        File destPath = new File(destDir, location);
        entry.setHash(hash);
        entry.setLocation(location);
        entry.setTo(to);
        entry.setSize(file.length());
        applicator.apply(entry);
        destPath.getParentFile().mkdirs();
        ClientFileCollector.log.info(String.format("Adding %s from %s...", relPath, file.getAbsolutePath()));
        if (copy) {
            Files.copy(file, destPath);
        }
        manifest.getTasks().add(entry);
    }

    public static DirectoryBehavior getDirectoryBehavior(@NonNull String name) {
        if (name.startsWith(".")) {
            return DirectoryBehavior.SKIP;
        } else if (name.equals("_OPTIONAL")) {
            return DirectoryBehavior.IGNORE;
        } else if (name.equals("_SERVER")) {
            return DirectoryBehavior.SKIP;
        } else if (name.equals("_CLIENT")) {
            return DirectoryBehavior.IGNORE;
        } else {
            return DirectoryBehavior.CONTINUE;
        }
    }

}
