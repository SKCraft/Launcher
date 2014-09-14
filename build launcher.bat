call mvn clean package
"C:\Program Files\Java\jdk1.7.0_67\bin\pack200.exe" --no-gzip target\DracoNetLauncher.jar.pack target\DracoNetLauncher.jar
copy  target\DracoNetLauncher.jar web\
copy  target\DracoNetLauncher.jar.pack web\
call mvn clean