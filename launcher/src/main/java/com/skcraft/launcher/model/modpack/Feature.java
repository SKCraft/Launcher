/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.modpack;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.google.common.base.Strings;
import lombok.Data;

@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="name")
@Data
public class Feature implements Comparable<Feature> {

    public enum Recommendation {
        STARRED,
        AVOID;

        @JsonCreator
        public static Recommendation fromJson(String text) {
            return valueOf(text.toUpperCase());
        }

        @JsonValue
        public String toJson() {
            return name().toLowerCase();
        };
    };

    private String name;
    private String description;
    private Recommendation recommendation;
    private boolean selected;

    public Feature() {
    }

    public Feature(String name, String description, boolean selected) {
        this.name = name;
        this.description = description;
        this.selected = selected;
    }

    public Feature(Feature feature) {
        setName(feature.getName());
        setDescription(feature.getDescription());
        setSelected(feature.isSelected());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public int compareTo(Feature o) {
        return Strings.nullToEmpty(getName()).compareTo(Strings.nullToEmpty(o.getName()));
    }
}
