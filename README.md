# GW2ML

[![License](https://img.shields.io/badge/license-MIT-green.svg?style=flat-square&label=License)](https://github.com/GW2Toolbelt/GW2ML/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.gw2tb.gw2ml/gw2ml.svg?style=flat-square&label=Maven%20Central)](https://maven-badges.herokuapp.com/maven-central/com.gw2tb.gw2ml/gw2ml)
[![JavaDoc](https://img.shields.io/maven-central/v/com.gw2tb.gw2ml/gw2ml.svg?style=flat-square&label=JavaDoc&color=blue)](https://javadoc.io/doc/com.gw2tb.gw2ml/gw2ml)
![Java](https://img.shields.io/badge/Java-8-green.svg?style=flat-square&color=b07219&logo=Java)

GW2ML is a Java library for fast and non-cached access to the data provided
by the Guild Wars 2 game client via the MumbleLink mechanism.

This library requires Java 8 or later and is fully compatible with the module
system introduced in Java 9.


## Usage

GW2ML provides an API that is designed to be as simple and intuitive to use as
possible while remaining efficient. The primary entry-point is [`MumbleLink.open()`](https://javadoc.io/doc/com.gw2tb.gw2ml/gw2ml/latest/com/gw2tb/gw2ml/MumbleLink.html)
which must be used to open a view of the MumbleLink data before anything can be
read. Once that is done the data can be accessed through various getter methods
on the returned view object.

A sample program that prints all data every few seconds to the command line can
be found [here](/src/test/java/com/example/Sample.java).
(This is also useful for testing whether your game client is writing any data.)


## Building from source

### Setup

This project uses [Gradle's toolchain support](https://docs.gradle.org/7.5/userguide/toolchains.html)
to detect and select the JDKs required to run the build. Please refer to the
build scripts to find out which toolchains are requested.

An installed JDK 1.8 (or later) is required to use Gradle.

### Building

Once the setup is complete, invoke the respective Gradle tasks using the
following command on Unix/macOS:

    ./gradlew <tasks>

or the following command on Windows:

    gradlew <tasks>

Important Gradle tasks to remember are:
- `clean`                   - clean build results
- `build`                   - assemble and test the project
- `publishToMavenLocal`     - build and install all public artifacts to the
                              local maven repository

Additionally `tasks` may be used to print a list of all available tasks.


## License

```
Copyright (c) 2019-2022 Leon Linhart

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```