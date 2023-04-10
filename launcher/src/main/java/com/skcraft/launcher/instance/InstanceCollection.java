package com.skcraft.launcher.instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.net.URL;
import java.util.List;

@Data
public class InstanceCollection extends InstanceNonSelectable {


    private final List<Instance> instances;
    @JsonIgnore private final URL manifestURL;

    public InstanceCollection(List<Instance> instances) {
        this.instances = instances;
        manifestURL = instances.get(0).getManifestURL();
    }

    /**
     * Get the tile of the instance, which might be the same as the
     * instance name if no title is set.
     *
     * @return a title
     */
    public String getTitle() {
        return instances.get(0).getTitle();
    }

    public boolean hasLocalInstance() {
        for (Instance i : instances) {
            if (i.isLocal()) {
                return true;
            }
        }
        return false;
    }

}
