package com.skcraft.launcher.install;

import com.skcraft.launcher.Launcher;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.skcraft.launcher.util.SharedLocale.tr;

@RequiredArgsConstructor
public class CreateLink implements InstallTask {
    private final File target;
    private final Path existing;

    @Override
    public void execute(Launcher launcher) throws Exception {
        Files.createSymbolicLink(target.toPath(), existing);
    }

    @Override
    public double getProgress() {
        return -1;
    }

    @Override
    public String getStatus() {
        return tr("installer.creatingLink", target, existing);
    }
}
