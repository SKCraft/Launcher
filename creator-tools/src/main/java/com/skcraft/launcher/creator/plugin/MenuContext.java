package com.skcraft.launcher.creator.plugin;

import com.google.common.util.concurrent.ListeningExecutorService;
import lombok.Data;

import java.awt.*;

@Data
public class MenuContext {
	private final Window owner;
	private final ListeningExecutorService executor;
}
