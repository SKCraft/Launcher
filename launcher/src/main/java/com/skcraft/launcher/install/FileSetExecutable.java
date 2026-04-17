package com.skcraft.launcher.install;

import com.skcraft.launcher.Launcher;
import lombok.RequiredArgsConstructor;

import java.io.File;

import static com.skcraft.launcher.util.SharedLocale.tr;

@RequiredArgsConstructor
public class FileSetExecutable implements InstallTask {
    private final File target;

    @Override
    public void execute(Launcher launcher) throws Exception {
        target.setExecutable(true, false);
    }

    @Override
    public double getProgress() {
        return -1;
    }

    @Override
    public String getStatus() {
        return tr("installer.settingExecutable", target);
    }
}
