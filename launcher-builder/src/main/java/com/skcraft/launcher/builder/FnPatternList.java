/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.builder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

@Data
public class FnPatternList {

    private static final EnumSet<FnMatch.Flag> DEFAULT_FLAGS = EnumSet.of(
            FnMatch.Flag.CASEFOLD, FnMatch.Flag.PERIOD);

    private List<String> include = Lists.newArrayList();
    private List<String> exclude = Lists.newArrayList();
    @Getter @Setter @JsonIgnore
    private EnumSet<FnMatch.Flag> flags = DEFAULT_FLAGS;

    public void setInclude(List<String> include) {
        this.include = include != null ? include : Lists.<String>newArrayList();
    }

    public void setExclude(List<String> exclude) {
        this.exclude = exclude != null ? exclude : Lists.<String>newArrayList();
    }

    public boolean matches(String path) {
        return include != null && matches(path, include) && (exclude == null || !matches(path, exclude));
    }

    public boolean matches(String path, Collection<String> patterns) {
        for (String pattern : patterns) {
            if (FnMatch.fnmatch(pattern, path, flags)) {
                return true;
            }
        }

        return false;
    }

}
