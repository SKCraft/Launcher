/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.util;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Indicates the platform.
 */
public enum Platform {
    @XmlEnumValue("windows") WINDOWS,
    @XmlEnumValue("mac_os_x") MAC_OS_X,
    @XmlEnumValue("linux") LINUX,
    @XmlEnumValue("solaris") SOLARIS,
    @XmlEnumValue("unknown") UNKNOWN
}