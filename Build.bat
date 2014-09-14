ECHO OFF
CLS
:MENU
ECHO.
ECHO ...............................................
ECHO PRESS 1, 2 OR 3 to select your task, or 4 to EXIT.
ECHO ...............................................
ECHO.
ECHO 1 - Build Launcher
ECHO 2 - Make Modpack
ECHO 3 - Clean
ECHO 4 - EXIT
ECHO.
SET /P M=Type 1, 2, 3, or 4 then press ENTER:
IF %M%==1 GOTO Launcher
IF %M%==2 GOTO Modpack
IF %M%==3 GOTO CLEAN
IF %M%==4 GOTO EOF
:Launcher
call mvn clean package
"C:\Program Files\Java\jdk1.7.0_67\bin\pack200.exe" --no-gzip target\DracoNetLauncher.jar.pack target\DracoNetLauncher.jar
copy  target\DracoNetLauncher.jar web\
copy  target\DracoNetLauncher.jar.pack web\
call mvn clean
GOTO MENU
:Modpack
java -cp web/DracoNetLauncher.jar com.skcraft.launcher.builder.PackageBuilder --version "1001" --manifest-dest "dist\dnet.json" --objects-dest "dist\dnet1710" --files "pack" --config "modpack.json" --objects-url "dnet1710" --version-file "1.7.10-Forge10.13.0.1208.json" --libs-url "http://file.dracomail.net/Modpack/Launcher/libraries"
GOTO MENU
:CLEAN
call mvn clean
GOTO MENU