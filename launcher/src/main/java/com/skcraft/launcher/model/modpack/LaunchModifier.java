/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.modpack;

import com.google.common.collect.Lists;
import com.skcraft.launcher.launch.JavaProcessBuilder;
import lombok.Data;

import java.util.List;

@Data
public class LaunchModifier {

    private List<String> flags = Lists.newArrayList();

    public void setFlags(List<String> flags) {
        this.flags = flags != null ? flags : Lists.<String>newArrayList();
    }

    public void modify(JavaProcessBuilder builder) {
        if (flags != null) {
            for (String flag : flags) {
                builder.getFlags().add(flag);
            }
        }
    }
}
