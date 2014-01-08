/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.modpack;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.skcraft.launcher.update.Installer;
import lombok.Data;
import lombok.ToString;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        defaultImpl = FileInstall.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FileInstall.class, name = "file")
})
@Data
@ToString(exclude = "manifest")
public abstract class Task implements Runnable {

    @JsonBackReference("manifest")
    private Manifest manifest;

    @JsonIgnore
    public Installer getInstaller() {
        return getManifest().getInstaller();
    }

}
