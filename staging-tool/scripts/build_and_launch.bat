@echo off

call build.bat
if ERRORLEVEL 0 goto Launch

:Launch
call launch.bat
goto End

:Error
echo "ERROR -- Modpack building failed!"
pause

:End