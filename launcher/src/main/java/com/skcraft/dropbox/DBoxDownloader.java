package com.skcraft.dropbox;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.skcraft.launcher.LauncherUtils.concat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.install.InstallLog;
import com.skcraft.launcher.install.InstallLogFileMover;
import com.skcraft.launcher.install.Installer;
import com.skcraft.launcher.install.UpdateCache;
import com.skcraft.launcher.launch.LaunchSupervisor;
import com.skcraft.launcher.model.modpack.FileInstall;
import com.skcraft.launcher.model.modpack.ManifestEntry;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.java.Log;
@Log
@Data
@EqualsAndHashCode(callSuper = false)
public class DBoxDownloader  extends ManifestEntry{
	
	
	
	
	
	
	
	
	
	  private static HashFunction hf = Hashing.sha1();
	    private String version;
	    private String hash;
	    private String location;
	    private String to;
	    private long size;
	    private boolean userFile;

	    @JsonIgnore
	    public String getImpliedVersion() {
	        return checkNotNull(version != null ? version : hash);
	    }

	    @JsonIgnore
	    public String getTargetPath() {
	        return checkNotNull(this.to != null ? this.to : location);
	    }

	    
	    public void install(@NonNull Installer installer, @NonNull InstallLog log,
	                        @NonNull UpdateCache cache, @NonNull File contentDir) throws IOException {
	        if (getWhen() != null && !getWhen().matches()) {
	            return;
	        }

	        String targetPath = getTargetPath();
	        File targetFile = new File(contentDir, targetPath);
	        String fileVersion = getImpliedVersion();
	        URL url = concat(getManifest().getObjectsUrl(), getLocation());

	        if (shouldUpdate(cache, targetFile)) {
	            long size = this.size;
	            if (size <= 0) {
	                size = 10 * 1024;
	            }

	            File tempFile = installer.getDownloader().download(url, fileVersion, size, to);
	            installer.queue(new InstallLogFileMover(log, tempFile, targetFile));
	        } else {
	            log.add(to, to);
	        }
	    }

	    private boolean shouldUpdate(UpdateCache cache, File targetFile) throws IOException {
	        if (targetFile.exists() && isUserFile()) {
	            return false;
	        }

	        if (!targetFile.exists()) {
	            return true;
	        }

	        if (hash != null) {
	            String existingHash = Files.hash(targetFile, hf).toString();
	            if (existingHash.equalsIgnoreCase(hash)) {
	                return false;
	            }
	        }

	        return cache.mark(FilenameUtils.normalize(getTargetPath()), getImpliedVersion());
	    }

	
	

}
