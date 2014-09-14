call mvn clean package
java -cp target/DracoNetLauncher.jar com.skcraft.launcher.builder.PackageBuilder --version "1001" --manifest-dest "dist\dnet.json" --objects-dest "dist\dnet1710" --files "pack" --config "modpack.json" --objects-url "dnet1710" --version-file "1.7.10-Forge10.13.0.1208.json" --libs-url "http://file.dracomail.net/Modpack/Launcher/libraries"
call mvn clean
