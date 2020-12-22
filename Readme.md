# Dolphin MMJR [2020/2021- REVAMP] 
[Using dolphin-emu as a base and Updating it with MMJ/MMJR performance tweaks]

[Visit their links below to support their work and see the original dolphin builds]

[Dolphin MMJR - A performance focused variant of Dolphin]

[Homepage](https://dolphin-emu.org/) | [Project Site](https://github.com/dolphin-emu/dolphin) | [Buildbot](https://dolphin.ci) | [Forums](https://forums.dolphin-emu.org/) | [Wiki](https://wiki.dolphin-emu.org/) | [Issue Tracker](https://bugs.dolphin-emu.org/projects/emulator/issues) | [Coding Style](https://github.com/dolphin-emu/dolphin/blob/master/Contributing.md) | [Transifex Page](https://www.transifex.com/projects/p/dolphin-emu/)

Dolphin is an emulator for running GameCube and Wii games on Windows,
Linux, macOS, and recent Android devices. It's licensed under the terms
of the GNU General Public License, version 2 or later (GPLv2+).

Please read the [FAQ](https://dolphin-emu.org/docs/faq/) before using Dolphin.

## System Requirements

### Android

* OS
    * Android (5.0 Lollipop or higher).
* Processor
    * A processor with support for 64-bit applications (either ARMv8 or x86-64).
* Graphics
    * A graphics processor that supports OpenGL ES 3.0 or higher. Performance varies heavily with [driver quality](https://dolphin-emu.org/blog/2013/09/26/dolphin-emulator-and-opengl-drivers-hall-fameshame/).
    * A graphics processor that supports standard desktop OpenGL features is recommended for best performance.

Dolphin can only be installed on devices that satisfy the above requirements. Attempting to install on an unsupported device will fail and display an error message.

## Building for Android

These instructions assume familiarity with Android development. If you do not have an
Android dev environment set up, see [AndroidSetup.md](AndroidSetup.md).

If using Android Studio, import the Gradle project located in `./Source/Android`.

Android apps are compiled using a build system called Gradle. Dolphin's native component,
however, is compiled using CMake. The Gradle script will attempt to run a CMake build
automatically while building the Java code.

## Command Line Usage

`Usage: Dolphin [-h] [-d] [-l] [-e <str>] [-b] [-V <str>] [-A <str>]`

* -h, --help Show this help message
* -d, --debugger Show the debugger pane and additional View menu options
* -l, --logger Open the logger
* -e, --exec=<str> Load the specified file (DOL,ELF,WAD,GCM,ISO)
* -b, --batch Exit Dolphin with emulator
* -V, --video_backend=<str> Specify a video backend
* -A, --audio_emulation=<str> Low level (LLE) or high level (HLE) audio

Available DSP emulation engines are HLE (High Level Emulation) and
LLE (Low Level Emulation). HLE is faster but less accurate whereas
LLE is slower but close to perfect. Note that LLE has two submodes (Interpreter and Recompiler)
but they cannot be selected from the command line.

Available video backends are "D3D" and "D3D12" (they are only available on Windows), "OGL", and "Vulkan".
There's also "Null", which will not render anything, and
"Software Renderer", which uses the CPU for rendering and
is intended for debugging purposes only.

## Sys Files

* `wiitdb.txt`: Wii title database from [GameTDB](https://www.gametdb.com/)
* `totaldb.dsy`: Database of symbols (for devs only)
* `GC/font_western.bin`: font dumps
* `GC/font_japanese.bin`: font dumps
* `GC/dsp_coef.bin`: DSP dumps
* `GC/dsp_rom.bin`: DSP dumps
* `Wii/clientca.pem`: Wii network certificate
* `Wii/clientcacakey.pem`: Wii network certificate
* `Wii/rootca.pem`: Wii network certificate

The DSP dumps included with Dolphin have been written from scratch and do not
contain any copyrighted material. They should work for most purposes, however
some games implement copy protection by checksumming the dumps. You will need
to dump the DSP files from a console and replace the default dumps if you want
to fix those issues.

Wii network certificates must be extracted from a Wii IOS. A guide for that can be found [here](https://wiki.dolphin-emu.org/index.php?title=Wii_Network_Guide).

## Folder Structure

These folders are installed read-only and should not be changed:

* `GameSettings`: per-game default settings database
* `GC`: DSP and font dumps
* `Maps`: symbol tables (dev only)
* `Shaders`: post-processing shaders
* `Themes`: icon themes for GUI
* `Resources`: icons that are theme-agnostic
* `Wii`: default Wii NAND contents

## Custom Textures

Custom textures have to be placed in the user directory under
`Load/Textures/[GameID]/`. You can find the Game ID by right-clicking a game
in the ISO list and selecting "ISO Properties".
