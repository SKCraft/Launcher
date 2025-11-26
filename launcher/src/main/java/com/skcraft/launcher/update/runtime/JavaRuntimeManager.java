package com.skcraft.launcher.update.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.install.*;
import com.skcraft.launcher.launch.runtime.JavaRuntime;
import com.skcraft.launcher.model.minecraft.JavaVersion;
import com.skcraft.launcher.model.minecraft.runtime.*;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.util.Environment;
import com.skcraft.launcher.util.HttpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

import static com.skcraft.launcher.util.HttpRequest.url;
import static com.skcraft.launcher.util.SharedLocale.tr;

@Log
@RequiredArgsConstructor
public class JavaRuntimeManager {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final File runtimesDir;

    public Optional<JavaRuntime> getRuntime(JavaVersion version) {
        File runtimeDir = new File(runtimesDir, version.getComponent());

        if (runtimeDir.isDirectory()) {
            RuntimeData info = Persistence.read(new File(runtimeDir, "runtime.json"), RuntimeData.class);

            return Optional.of(new JavaRuntime(runtimeDir, info.getVersion(), info.is64Bit()));
        } else {
            return Optional.empty();
        }
    }

    public void install(
            Installer installer,
            URL manifestUrl,
            JavaVersion version
    ) throws IOException, InterruptedException {
        // Only download if the runtime is missing
        // TODO: Support for marking runtime as corrupt?
        if (this.getRuntime(version).isPresent()) return;

        File destination = new File(runtimesDir, version.getComponent());
        ObjectNode node = HttpRequest.get(manifestUrl)
                .execute()
                .expectResponseCode(200)
                .returnContent()
                .asJson(ObjectNode.class)
                .without("gamecore");
        RuntimeList availableRuntimes = mapper.treeToValue(node, RuntimeList.class);

        RuntimePlatform platform = RuntimePlatform.from(Environment.getInstance());
        RuntimeInfo info = availableRuntimes.getRuntime(platform, version);
        if (info == null) {
            log.warning("Failed to match an available Java runtime - unsupported platform?");
            return;
        }

        DownloadInfo downloadInfo = info.getManifest();
        RuntimeManifest manifest = HttpRequest
                .get(url(downloadInfo.getUrl()))
                .execute()
                .expectResponseCode(200)
                .returnContent()
                .asJson(RuntimeManifest.class);

        Downloader downloader = installer.getDownloader();
        manifest.getFiles().forEach((filename, entry) -> {
            File target = new File(destination, filename);
            if (entry instanceof RuntimeManifestEntry.File) {
                RuntimeManifestEntry.File file = (RuntimeManifestEntry.File) entry;
                DownloadInfo fileDownload = file.getDownloads().get(Format.RAW);

                File tempFile = downloader.download(url(fileDownload.getUrl()), "", fileDownload.getSize(), filename);
                installer.queue(new FileMover(tempFile, target));

                if (fileDownload.getSha1() != null) {
                    installer.queue(new FileVerify(target, filename, fileDownload.getSha1()));
                }

                if (file.isExecutable()) {
                    installer.queue(new FileSetExecutable(target));
                }
            } else if (entry instanceof RuntimeManifestEntry.Directory) {
                installer.queue(new CreateFolder(target));
            } else if (entry instanceof RuntimeManifestEntry.Link) {
                String relpath = ((RuntimeManifestEntry.Link) entry).getTarget();
                Path existing = target.toPath().getParent().resolve(relpath);

                installer.queue(new CreateLink(target, existing));
            }
        });

        // Write runtime data
        installer.queueLate(new InstallTask() {
            @Override
            public void execute(Launcher launcher) throws Exception {
                RuntimeData rd = new RuntimeData(version.getComponent(), info.getVersion().getName(), platform.is64Bit());
                Persistence.write(new File(destination, "runtime.json"), rd);
            }

            @Override
            public double getProgress() {
                return -1;
            }

            @Override
            public String getStatus() {
                return tr("installer.adhoc.writingRuntimeData");
            }
        });
    }
}
