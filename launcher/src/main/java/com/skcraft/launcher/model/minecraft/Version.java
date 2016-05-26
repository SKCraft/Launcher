/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.minecraft;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Version {

    @Getter
    @Setter
    @NonNull
    private String id;

    public Version() {
    }

    public Version(@NonNull String id) {
        this.id = id;
    }

    @JsonIgnore
    public String getName() {
        return id;
    }

    @Override
    public String toString() {
        return getName();
    }

    boolean thisEquals(Version other) {
        return getId().equals(other.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Version version = (Version) o;
        return thisEquals(version) && version.thisEquals(this);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
