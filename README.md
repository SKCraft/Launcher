SKCraft Launcher
================

This project provides an open-source Minecraft launcher platform for downloading,
installing, and updating modpacks.

The launcher has its roots in MC Alpha as a launcher for sk89q's server.

**Note:** "SKMCLauncher" is the *older* version of this launcher. This project is called "SKCraft Launcher."

## Introduction

* Requires almost no configuration files to make a modpack
* Add a new mod by dropping in the .jar (and its configuration)
* Remove a mod by deleting its .jar (and configuration).
* Builds **server** modpacks with no extra configuration
* Advanced download system: incremental, file removal detection, optional feature/mod selection, etc.
* Very easy for users to use and install modpacks
* Open source!

## Usage

1. Download the code.
2. See if you can compile it (see instructions below).
3. Read the documentation to (1) learn how to change the launcher to use your own website and (2) create modpacks in the right format for the launcher.

* [Documentation](http://wiki.sk89q.com/wiki/Launcher)
* [Forum to ask for help](http://forum.enginehub.org/forums/launcher.25/)

You can also [contact sk89q](http://www.sk89q.com/contact/).

## Compiling

First, make sure to install the Java Development Kit (JDK).

In your command prompt or terminal, run:

	./gradlew clean build

If you are on Windows:

	gradlew clean build

Once compiled, look for the "-all" .jar files in the following folders:

* `launcher/build/libs/` - The main launcher
* `launcher-builder/build/libs/` - Command line app to build modpacks

If you wish to import the project into an IDE, you must add support for Project Lombok (IntelliJ IDEA users: also enable annotation processing in compiler settings).

## Contributing

Pull requests can be submitted on GitHub, but we will accept them at our discretion. Please note that your code must follow Oracle's Java Code Conventions.

Contributions by third parties must be dual licensed under the two licenses described within LICENSE.txt (GNU General Public License, version 3, and the 3-clause BSD license).

## License

The launcher is licensed under the GNU General Public License, version 3.
