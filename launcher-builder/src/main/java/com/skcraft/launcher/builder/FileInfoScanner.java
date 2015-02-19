/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skcraft.launcher.model.modpack.Feature;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;
import static com.skcraft.launcher.builder.ClientFileCollector.getDirectoryBehavior;
import static org.apache.commons.io.FilenameUtils.*;

@Log
public class FileInfoScanner extends DirectoryWalker {

    private static final EnumSet<FnMatch.Flag> MATCH_FLAGS = EnumSet.of(
            FnMatch.Flag.CASEFOLD, FnMatch.Flag.PERIOD, FnMatch.Flag.PATHNAME);
    public static final String FILE_SUFFIX = ".info.json";

    private final ObjectMapper mapper;
    @Getter
    private final List<FeaturePattern> patterns = new ArrayList<FeaturePattern>();

    public FileInfoScanner(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected DirectoryBehavior getBehavior(String name) {
        return getDirectoryBehavior(name);
    }

    @Override
    protected void onFile(File file, String relPath) throws IOException {
        if (file.getName().endsWith(FILE_SUFFIX)) {
            String fnPattern =
                    separatorsToUnix(getPath(relPath)) +
                    getBaseName(getBaseName(file.getName())) + "*";

            FileInfo info = mapper.readValue(file, FileInfo.class);
            Feature feature = info.getFeature();

            if (feature != null) {
                checkNotNull(emptyToNull(feature.getName()),
                        "Empty component name found in " + file.getAbsolutePath());

                List<String> patterns = new ArrayList<String>();
                patterns.add(fnPattern);
                FnPatternList patternList = new FnPatternList();
                patternList.setInclude(patterns);
                patternList.setFlags(MATCH_FLAGS);
                FeaturePattern fp = new FeaturePattern();
                fp.setFeature(feature);
                fp.setFilePatterns(patternList);
                getPatterns().add(fp);

                FileInfoScanner.log.info("Found .info.json file at " + file.getAbsolutePath() +
                        ", with pattern " + fnPattern + ", and component " + feature);
            }
        }
    }

}
