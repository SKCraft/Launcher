/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator.model.creator;

import lombok.Data;

@Data
public class Problem {

    private final String title;
    private final String explanation;

    public Problem(String title, String explanation) {
        this.title = title;
        this.explanation = explanation;
    }

    @Override
    public String toString() {
        return title;
    }

}
