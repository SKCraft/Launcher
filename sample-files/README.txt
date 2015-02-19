These are sample files for a project.

- lightpack/ is one modpack
- monsterpack/ is another modpack
- upload/ is what you'd upload to your website

In upload:

- "latest.json" was written by hand
- "packages.php" automatically generates the package listing, but your web server needs PHP installed to use it
- "packages.json" is what packages.php would have generated, but you can write it by hand instead
- "news.html" is your news page

Commands
========

The following commands were run to generate the modpack files in upload/

java -jar ../launcher-builder/build/libs/launcher-builder-4.2.3-SNAPSHOT-all.jar --version 20100223 --input lightpack --output upload --manifest-dest "upload\lightpack.json" --pretty-print

java -jar ../launcher-builder/build/libs/launcher-builder-4.2.3-SNAPSHOT-all.jar --version 20100223 --input monsterpack --output upload --manifest-dest "upload\monsterpack.json" --pretty-print

launcher.properties
===================

If you had uploaded the contents of upload/ to http://example.com/launcher/, you would make launcher.properties look like this:

newsUrl=http://example.com/launcher/news.html?version=%s
packageListUrl=http://example.com/launcher/packages.php?key=%s
selfUpdateUrl=http://example.com/launcher/latest.json

(note: it uses the .php version here)