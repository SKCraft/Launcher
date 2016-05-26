/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.model.modpack;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="if")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RequireAny.class, name = "requireAny"),
        @JsonSubTypes.Type(value = RequireAll.class, name = "requireAll")
})
public interface Condition {

    boolean matches();

}
