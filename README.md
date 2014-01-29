SKCraft Launcher
================

This launcher features a powerful package deployment system for 
Minecraft. We use it to provide updates for our server, and you may 
find it useful too.

Unlike the previous iteration of the launcher,
due to time constraints, we have gone back to making launchers suited for
one server (or one provider), meaning that you will have to modify the
launcher as necessary and distribute your own .exe, .jar, .app files
to fit your requirements. However, this is fairly easy to do because
nearly every string has been translated, and all the important
properties reside in one single file. See [the 
relevant page](http://confluence.skcraft.com/display/LAUN/Launcher+Customization) 
for more information. You are invited to fork this repository on
GitHub and make the necessary changes.

For the time being, the launcher is no longer a general purpose
Minecraft launcher.

* [Home page](http://opensource.skcraft.com/)
* [Documentation](http://confluence.skcraft.com/display/LAUN/Launcher)
* [Issue tracker](http://issues.skcraft.com/browse/LAUN)

Architectural Changes from 3.x
------------------------------

This launcher has gone through two major updates since the days
of Minecraft Alpha, but the update system (and its features) has been carried
over between launcher versions. The biggest change from 
[version 3.x](https://github.com/sk89q/skmclauncher) is perhaps the changeover
to JSON from XML due to the new requirement that a JSON parsing
library must be bundled with the launcher (we use Jackson JSON). Prior
versions of the launcher utilized the JAXB implementation bundled with
the Java runtime.

Also note that we've decided to bundle additional libraries (notably
Guava), which brings the file size of the launcher up by a few
megabytes, which is fairly negligible.

Compiling
---------

The launcher can be compiled using [Maven](http://maven.apache.org/).

    mvn clean package

If you wish to import the project into an IDE, you must add support for
Project Lombok.

Contributing
------------

Pull requests can be submitted on GitHub, but we will accept them
at our discretion. Please note that your code must follow
Oracle's Java Code Conventions.

Contributions by third parties must be dual licensed under the two licenses
described within LICENSE.txt (GNU General Public License, version 3, and the
3-clause BSD license).


License
-------

The launcher is licensed under the GNU General Public License, version 3.
