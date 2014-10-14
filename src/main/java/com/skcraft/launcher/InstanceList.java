/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import com.skcraft.concurrency.DefaultProgress;
import com.skcraft.concurrency.ProgressObservable;
import static com.skcraft.launcher.LauncherUtils.concat;
import com.skcraft.launcher.model.modpack.ManifestInfo;
import com.skcraft.launcher.model.modpack.PackageList;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.util.HttpRequest;
import static com.skcraft.launcher.util.SharedLocale._;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;
import nz.co.lolnet.james137137.PrivatePrivatePackagesManager;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

/**
 * Stores the list of instances.
 */
@Log
public class InstanceList {

    private final Launcher launcher;
    @Getter private final List<Instance> instances = new ArrayList<Instance>();

    /**
     * Create a new instance list.
     *
     * @param launcher the launcher
     */
    public InstanceList(@NonNull Launcher launcher) {
        this.launcher = launcher;
    }

    /**
     * Get the instance at a particular index.
     *
     * @param index the index
     * @return the instance
     */
    public synchronized Instance get(int index) {
        return instances.get(index);
    }

    /**
     * Get the number of instances.
     *
     * @return the number of instances
     */
    public synchronized int size() {
        return instances.size();
    }

    /**
     * Create a worker that loads the list of instances from disk and from
     * the remote list of packages.
     *
     * @return the worker
     */
    public Enumerator createEnumerator() {
        return new Enumerator();
    }

    /**
     * Get a list of selected instances.
     *
     * @return a list of instances
     */
    public synchronized List<Instance> getSelected() {
        List<Instance> selected = new ArrayList<Instance>();
        for (Instance instance : instances) {
            if (instance.isSelected()) {
                selected.add(instance);
            }
        }

        return selected;
    }

    /**
     * Sort the list of instances.
     */
    public synchronized void sort() {
        Collections.sort(instances);
    }

    public final class Enumerator implements Callable<InstanceList>, ProgressObservable {
        private ProgressObservable progress = new DefaultProgress(-1, null);

        private Enumerator() {
        }

        @Override
        public InstanceList call() throws Exception {
            log.info("Enumerating instance list...");
            progress = new DefaultProgress(0, _("instanceLoader.loadingLocal"));

            List<Instance> local = new ArrayList<Instance>();
            List<Instance> remote = new ArrayList<Instance>();

            File[] dirs = launcher.getInstancesDir().listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
            if (dirs != null) {
                for (File dir : dirs) {
                    File file = new File(dir, "instance.json");
                    Instance instance = Persistence.load(file, Instance.class);
                    instance.setDir(dir);
                    instance.setName(dir.getName());
                    instance.setSelected(true);
                    instance.setLocal(true);
                    local.add(instance);

                    log.info(instance.getName() + " local instance found at " + dir.getAbsolutePath());
                }
            }

            progress = new DefaultProgress(0.3, _("instanceLoader.checkingRemote"));

            try {
                URL packagesURL = launcher.getPackagesURL();

                PackageList packages = HttpRequest
                        .get(packagesURL)
                        .execute()
                        .expectResponseCode(200)
                        .returnContent()
                        .asJson(PackageList.class);
                PrivatePrivatePackagesManager.addPrivatePackages(packages);
                if (packages.getMinimumVersion() > Launcher.PROTOCOL_VERSION) {
                    throw new LauncherException("Update required", _("errors.updateRequiredError"));
                }

                for (ManifestInfo manifest : packages.getPackages()) {
                    boolean foundLocal = false;

                    for (Instance instance : local) {
                        if (instance.getName().equalsIgnoreCase(manifest.getName())) {
                            foundLocal = true;

                            instance.setTitle(manifest.getTitle());
                            instance.setPriority(manifest.getPriority());
                            URL url = concat(packagesURL, manifest.getLocation());
                            instance.setManifestURL(url);

                            log.info("(" + instance.getName() + ").setManifestURL(" + url + ")");

                            // Check if an update is required
                            if (instance.getVersion() == null || !instance.getVersion().equals(manifest.getVersion())) {
                                instance.setUpdatePending(true);
                                instance.setVersion(manifest.getVersion());
                                Persistence.commitAndForget(instance);
                                log.info(instance.getName() + " requires an update to " + manifest.getVersion());
                            }
                        }
                    }

                    if (!foundLocal) {
                        File dir = new File(launcher.getInstancesDir(), manifest.getName());
                        File file = new File(dir, "instance.json");
                        Instance instance = Persistence.load(file, Instance.class);
                        instance.setDir(dir);
                        instance.setTitle(manifest.getTitle());
                        instance.setName(manifest.getName());
                        instance.setVersion(manifest.getVersion());
                        instance.setPriority(manifest.getPriority());
                        instance.setSelected(false);
                        instance.setManifestURL(concat(packagesURL, manifest.getLocation()));
                        instance.setUpdatePending(true);
                        instance.setLocal(false);
                        remote.add(instance);

                        log.info("Available remote instance: '" + instance.getName() +
                                "' at version " + instance.getVersion());
                    }
                }
            } catch (IOException e) {
                throw new IOException("The list of modpacks could not be downloaded.", e);
            } finally {
                synchronized (InstanceList.this) {
                    instances.clear();
                    instances.addAll(local);
                    instances.addAll(remote);

                    log.info(instances.size() + " instance(s) enumerated.");
                }
            }

            return InstanceList.this;
        }

        @Override
        public double getProgress() {
            return -1;
        }

        @Override
        public String getStatus() {
            return progress.getStatus();
        }
    }
}
