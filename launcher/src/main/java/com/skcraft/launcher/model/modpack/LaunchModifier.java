/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.modpack;

import com.skcraft.launcher.launch.JavaProcessBuilder;
import lombok.Data;

import java.util.List;

@Data
public class LaunchModifier {

    private List<String> flags;

    public void modify(JavaProcessBuilder builder) {
        if (flags != null) {
            for (String flag : flags) {
                builder.getFlags().add(flag);
            }
        }
    }
}
