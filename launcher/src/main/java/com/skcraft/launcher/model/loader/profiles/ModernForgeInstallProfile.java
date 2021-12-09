/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.loader.profiles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.skcraft.launcher.model.loader.InstallProcessor;
import com.skcraft.launcher.model.loader.ProcessorEntry;
import com.skcraft.launcher.model.loader.SidedData;
import com.skcraft.launcher.model.minecraft.Library;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModernForgeInstallProfile {
    private int spec;
    private List<Library> libraries;
    private List<InstallProcessor> processors;
    private Map<String, SidedData<String>> data;
    private String minecraft;

    public List<ProcessorEntry> toProcessorEntries(final String loaderName) {
        return Lists.transform(getProcessors(), new Function<InstallProcessor, ProcessorEntry>() {
            @Override
            public ProcessorEntry apply(InstallProcessor input) {
                return new ProcessorEntry(loaderName, input);
            }
        });
    }
}
