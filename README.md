SKCraft Launcher
================

SKCraft Launcher provides a platform for creating, testing, and distributing modpacks for groups of all sizes. It 100% open source and works on Windows, Mac OS X and Linux/BSD.

With this platform, you have **full control** of the modpacks that you choose to release.

### Many Features, Few Requirements

The only requirement is **have a website to host files — even Dropbox will work.** All files that you upload are "de-duplicated" so even if you have a dozen modpacks that use the same mod, you only have to host one copy of the file.

Yet you still have access to powerful features like:

* optional mods/files
* pre-installed default configuration files
* modpack-added Java flags
* JAR mods
* resource pack support
* non-standard modpack file support
* "hidden" modpacks that require a special access key **

** requires PHP support on your website

### Making Modpacks

Making modpacks is extremely easy: you can use our creator tools with 1-click modpack testing:

![Modpack Creator](readme/pack_manager.png)

You *don't* have to click a bunch of buttons or edit several cryptic config files to add a mod. All you need to do is create a folder that resembles the Minecraft data folder:

* src/**config**/
* src/**mods**/
* src/**resourcepacks**/
* loaders/

There's support for Minecraft Forge and LiteLoader too — just put their installer .jars into the loaders folder.

Now let's see how easy it is to create an "optional feature" that can turn off not only mods, but configuration files too:

![Optional Features Configuration](readme/configure_features.png)

### Great Client

The launcher has a beautiful dark UI that's elegant and easy to use (although a native UI is also available). Display your own webpages so users always know what's up when they start the game.

![Skinned](readme/launcher_skinned.png)

When it comes to launching your modpacks, users can start, abort, and resume updates at any time. Files are downloaded in parallel, and the launcher knows how to handle the removal of files from the modpack.

Users can select those optional features that you have added:

![Optional Features](readme/features.png)

The launcher can **even update itself**.

...and you can use it in portable mode too.

### More Features

All of the modpack tools can be used to create a server modpack alongside the client modpack!

You can optionally use **command line tools** only, without the GUI entirely. Combine the launcher with Git and Jenkins (or whatever you prefer) to automatically build modpack releases when you push a new version.

### History

The launcher has been in development since the early days of Minecraft Alpha. It has powered and always has powered sk89q's own server, SKCraft.

[The older 3.x version was can be found elsewhere](https://github.com/sk89q/SKMCLauncher).

## Additional Screenshots

You can also use the "plain" version of the launcher:

![Main Launcher](readme/launcher.png)

![Options](readme/options.png)

![Console](readme/log.png)

More of the Modpack Creator:

![Modpack Creator](readme/modpack_creator.png)

![Build Modpacks](readme/packages_generator.png)

## Getting Started

* [Read the wiki](https://github.com/SKCraft/Launcher/wiki)
* [Forum to ask for help](http://forum.enginehub.org/forums/launcher.25/)
* [Join us on IRC: #sklauncher on EsperNet (irc.esper.net)](https://webchat.esper.net/?channels=sklauncher)

If you find the launcher useful, you can [support me on Patreon](https://www.patreon.com/sk89q).

[![Support Me on Patreon](https://i.imgur.com/Sg03Bzc.png)](https://www.patreon.com/sk89q)

## Compiling

In your command prompt or terminal, run:

	./gradlew clean build

If you are on Windows:

	gradlew clean build

If you wish to import the project into an IDE, you must add support for Project Lombok (IntelliJ IDEA users: also enable annotation processing in compiler settings).

## Contributing

Pull requests can be submitted on GitHub, but we will accept them at our discretion. Please note that your code must follow Oracle's Java Code Conventions.

Contributions by third parties must be dual licensed under the two licenses described within LICENSE.txt (GNU Lesser General Public License, version 3, and the 3-clause BSD license).

## License

The launcher is licensed under the GNU Lesser General Public License, version 3.
