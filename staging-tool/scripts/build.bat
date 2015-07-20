@echo off

java -cp staging-tool.jar com.skcraft.launcher.builder.PackageBuilder --version "%DATE% %TIME%" --input ..\ --output www --manifest-dest "www/staging.json" 