package com.skcraft.launcher.util;

import java.io.IOException;

@FunctionalInterface
public interface HttpFunction<T, V> {
	V call(T arg) throws IOException, InterruptedException;
}
