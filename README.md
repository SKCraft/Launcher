Lolnet Launcher 
[![Build Status](https://travis-ci.org/James137137/LolnetLauncher.svg?branch=master)](https://travis-ci.org/James137137/LolnetLauncher)
================

This project is forked from SKCraft Launcher that provides an open-source Minecraft launcher platform for downloading,
installing, and updating modpacks.

I james137137 do not claim I am the author of SKCraft launcher. I have renamed SKCraft launcher
to Lolnet launcher as I have customized it for the best interest of Lolnet.

All the changes have been committed and I (james137137) give full ownership to myself for my work
unless otherwise stated.

The main changes I have made are:

* Moved all user data to %appdata%/LolnetData which is changeable via options.
* Added an auto updater.
* Changed the look&feel and window size.
* Made sure that the launcher opens the latest java and at 64 bit when possible.
* Automatic calculate how much memory can be used.
* Added an extra button to show lolnet's server status.
* Allowed private pack codes.
* Added a .exe wrapper on build.
* Allowed minecraft.jar not just custom_jar.jar in the src folder.
* Changed ID/password to Minecraft username/email in the login section.
* Added a smart way to detect how much memory is available (with most modpacks this will need to be adjusted by player).
* Added button to filter public and private packs.
* Added some statistics collection (number of times a modpack is launched).
* By Right clicking on a modpack you can view the changelog.
* Removed some unwanted buttons and options (some were confusing).
* Modpacks can have custom icons.
* Updated Webpanel to HTML5 (launcher requires Java 8).
* Added a Help button for FAQ that can redirect to webpages if needed.
* When clicking on a modpack it can (if available) display a news section just for that pack.



Introduction
------------

SKCraft launcher is maintained by sk89q, who writes WorldEdit, WorldGuard, and so on. It has
been primarily developed for his server, but you can use it for your own modpack or
server.

* One of Minecraft's oldest launchers -- since Minecraft Alpha
* Requires almost no configuration files to make a modpack
* Add a new mod by dropping in the .jar (and its configuration)
* Remove a mod by deleting its .jar (and configuration).
* Builds **server** modpacks with no extra configuration
* Advanced download system: incremental, file removal detection, optional feature/mod selection, etc.
* Very easy for users to use and install modpacks
* Pretty well-documented with easy-to-understand, well-organized code*
* Open source!

*Except for the Launcher frame class. That one is pretty bad.

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
