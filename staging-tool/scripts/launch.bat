@echo off

mkdir launcher
cd launcher
java -cp ..\staging-tool.jar com.skcraft.launcher.staging.StagingServer --www-dir ..\www --launch