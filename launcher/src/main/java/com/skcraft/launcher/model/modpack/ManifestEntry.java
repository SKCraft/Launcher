/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.modpack;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.skcraft.launcher.install.InstallExtras;
import com.skcraft.launcher.install.InstallLog;
import com.skcraft.launcher.install.Installer;
import com.skcraft.launcher.install.UpdateCache;
import com.skcraft.launcher.model.loader.ProcessorEntry;
import lombok.Data;
import lombok.ToString;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        defaultImpl = FileInstall.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FileInstall.class, name = "file"),
        @JsonSubTypes.Type(value = ProcessorEntry.class, name = "process")
})
@Data
@ToString(exclude = "manifest")
public abstract class ManifestEntry {

    @JsonBackReference("manifest")
    private Manifest manifest;
    private Condition when;

    public abstract void install(Installer installer, InstallLog log, UpdateCache cache, InstallExtras extras) throws Exception;

}
