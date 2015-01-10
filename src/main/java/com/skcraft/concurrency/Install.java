package com.skcraft.concurrency;

import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.install.Installer;
import com.skcraft.launcher.update.BaseUpdater;
import lombok.NonNull;

/**
 * @author dags_ <dags@dags.me>
 */

public class Install extends BaseUpdater
{
    public static Install getInstall()
    {
        return new Install(Launcher.instance);
    }

    protected Install(@NonNull Launcher launcher)
    {
        super(launcher);
    }

    public void installPackage()
    {
        Installer i = new Installer(Launcher.instance.getTemporaryDir());
        try
        {
            super.installPackage(i, Launcher.instance.getInstances().get(0));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
