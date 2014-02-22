SKCraft Launcher
================

This project provides an open-source Minecraft launcher platform for downloading,
installing, and updating modpacks.

Introduction
------------

We've maintained this launcher since the days of Minecraft Alpha (circa 2010) for sk89q's
(who you may know as the author of WorldEdit, WorldGuard, etc.) Minecraft server.
It continues to be maintained because other launchers are more difficult
to use, require more work to do everything, have fewer features, have come and gone,
and are not open source. (Plus, we can brand the entire launcher as needed and have full
control over its direction.)

* Users can very easily select and install modpacks. The interface makes it fairly
  clear the available options.
* When building the modpack, convention is preferred over configuration. We do not like
  having to maintain configuration files (or perhaps just I).
* The server modpack can be built from the same folder. (On previous launcher versions, we
  accomplished this with a separate Python script that was not released. It was a pretty
  old script, because it was made for the days when you had separate client and server 
  .jar files for mods.)
* The update system is extremely powerful and versatile, with incremental updates, file
  removal detection, optional feature/mod selection, and so on.

For those are familiar with the 3.x launcher series, be aware that 3.x also served
as a general-purpose launcher. Unfortunately, that also split time away from "making a good
modpack/server launcher" and so that goal has been suspended for the time being. With
that in mind, you **no longer** ask users to install URLs for individual modpacks; rather,
you provide a URL that has a list of all available modpacks. This was a long desired
feature, but was never done due to time constraints.

This repository holds the 4.x version. As you may know, MC 1.6 changed how the
game was launched. Since the 3.x codebase was fairly ancient, the launcher was rewritten
to support MC 1.6 but primarily to also clean up the code. XML was dropped because
we have to bundle a JSON library now anyway, and we all know how much everyone hates XML.

Lastly, the source in this repository is the one for my (sk89q) server, so you will have
to remove the branding and change some property files. All of this is documented on the wiki.

* [Home page](http://opensource.skcraft.com/)
* [Documentation](http://confluence.skcraft.com/display/LAUN/Launcher)
* [Issue tracker](http://issues.skcraft.com/browse/LAUN)

Note that documentation may be lacking in some places. If you run into problems,
**do not be hesistant to ask**. While the launcher versions have always had many features,
documentation has always been lacking due to time constraints.

The launcher also exists primarily for my server, but we try to make as many resources
available as possible. The launcher will be supported for some time. If you
want to contact me about some sort of partnership, want to make the launcher the official
launcher for something, please email me (see [my website](http://www.sk89q.com/contact/)).
While you do not have to do this, I can make future decisions with awareness of what
the needs of other users may be.

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
