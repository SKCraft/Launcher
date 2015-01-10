package com.skcraft.launcher.update;

import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.dialog.ProgressDialog;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author dags_ <dags@dags.me>
 */

public class SelfInstallUpdate
{

    private final Window owner;
    private final JDialog parent;

    public SelfInstallUpdate(Window w, JDialog parent)
    {
        owner = w;
        this.parent = parent;
    }

    public void download()
    {
        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                preCleanUp();
                Updater u = new Updater(Launcher.instance, Launcher.instance.getInstances().get(0));
                u.setOnline(true);
                u.setManual(true);
                final ProgressDialog pd = new ProgressDialog(owner, "Self Install", "Downloading...");
                Thread t1 = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        pd.setVisible(true);
                    }
                });
                try
                {
                    t1.start();
                    u.call();
                    postCleanUp();
                    writeInstructions();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    pd.setVisible(false);
                    t1.interrupt();
                    u.setManual(false);


                    parent.dispose();
                }
            }
        });
        t.start();
    }

    private void preCleanUp()
    {
        Launcher l = Launcher.instance;
        File installFilesDir = new File(l.getBaseDir(), "Install Files");
        File profileDir = l.getInstances().get(0).getContentDir();
        File tempDir = l.getTemporaryDir();
        File versionDir = l.getVersionsDir();
        cleanUp(installFilesDir);
        cleanUp(profileDir);
        cleanUp(tempDir);
        cleanUp(versionDir);
    }

    private void postCleanUp()
    {
        Launcher l = Launcher.instance;
        File installFilesDir = new File(l.getBaseDir(), "Install Files");
        File profileDir = l.getInstances().get(0).getContentDir();
        File tempDir = l.getTemporaryDir();
        File versionDir = l.getVersionsDir();

        cleanUp(installFilesDir);
        cleanUp(tempDir);
        cleanUp(versionDir);

        File[] listFiles = profileDir.listFiles();
        if (listFiles != null)
        {
            for (File f : listFiles)
            {
                if (f.isFile())
                {
                    f.delete();
                }
            }
        }
        profileDir.renameTo(installFilesDir);
        cleanUp(l.getInstancesDir());
    }

    private void writeInstructions()
    {
        try
        {
            File f = new File(Launcher.instance.getBaseDir(), "README.txt");
            if (f.exists())
                return;
            f.createNewFile();
            FileWriter fw = new FileWriter(f);
            fw.write(readMe());
            fw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void cleanUp(File f)
    {
        if (f.exists())
        {
            deleteFolder(f);
        }
    }

    private void deleteFolder(File f)
    {
        if (!f.exists())
        {
            return;
        }
        File[] listFiles = f.listFiles();
        if (listFiles != null)
        {
            for (File fs : listFiles)
            {
                if (fs.isDirectory())
                {
                    deleteFolder(fs);
                }
                else
                {
                    fs.delete();
                }
            }
        }
        f.delete();
    }

    private String readMe()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("1. Run a CLEAN install of vanilla minecraft 1.7.10").append("\n");
        sb.append("2. Install the latest recommended version of Forge for minecraft 1.7.10").append("\n");
        sb.append("3. Copy the 3 folders included within 'Install Files' to your minecraft installation").append("\n");
        sb.append("Example:").append("\n");
        sb.append("/.minecraft/acblockspack").append("\n");
        sb.append("/.minecraft/mods").append("\n");
        sb.append("/.minecraft/resourcepacks").append("\n");
        return sb.toString();
    }

}
