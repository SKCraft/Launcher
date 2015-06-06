/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.install;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class FeatureCache {

    private Map<String, Boolean> selected = new HashMap<String, Boolean>();

}
