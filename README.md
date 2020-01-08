# GW2ML

[![License](https://img.shields.io/badge/license-MIT-green.svg?style=flat-square&label=License)](https://github.com/GW2Toolbelt/GW2ML/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.gw2toolbelt.gw2ml/gw2ml.svg?style=flat-square&label=Maven%20Central)](https://maven-badges.herokuapp.com/maven-central/com.github.gw2toolbelt.gw2ml/gw2ml)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.gw2toolbelt.gw2ml/gw2ml.svg?style=flat-square&label=JavaDoc&color=blue)](https://javadoc.io/doc/com.github.gw2toolbelt.gw2ml/gw2ml)

GW2ML is a Java library for fast and non-cached access to the data provided
by the Guild Wars 2 game client via the MumbleLink mechanism.

This library supports Java 8 and is fully compatible with the module system
introduced in Java 9.


## Usage

### Setup

GW2ML consists of multiple artifacts:

- `gw2ml.jar` (The main library.)
- `gw2ml-sources.jar` (The source archive.)
- `gw2ml-javadoc.jar` (The JavaDoc archive.)
- `gw2ml-natives-<platform>.jar` (The platform-specific prebuilt native libraries.)

To compile an application using GW2ML, the base artifact should be added to the
class-path (or the module-path). When running an application GW2ML requires a
platform-specific native library to be available. GW2ML extracts the native
library to a temporary folder and loads them automatically if a native artifact
is on the class-path (or module-path). If more customization is required (e.g.
when creating a platform-specific installer) the natives may be extracted
manually and loaded via `java.library.path`. See the [Configuration](/src/main/java/com/github/gw2toolbelt/gw2ml/Configuration.java)
class for more options.

GW2ML provides prebuilt artifacts for all supported platforms. The currently
supported platforms/architectures are:

- Windows x64 (`gw2ml-natives-windows.jar`)


### Accessing MumbleLink data

GW2ML provides an API that is designed to be as simple and intuitive to use as
possible while remaining efficient. The primary entry-point is [`MumbleLink.open()`](https://github.com/TheMrMilchmann/GW2ML/blob/master/src/main/java/com/github/gw2toolbelt/gw2ml/MumbleLink.java#L89)
which must be used to open a view of the MumbleLink data before anything can be
read. Once that is done the data can be accessed through various getter methods
on the returned view object.

A sample program that prints all data every few seconds to the command line can
be found [here](/src/test/java/com/example/Sample.java).
(This is also useful for testing whether your game client is writing any data.)


## Building from source

### Setup

A complete build expects multiple JDK installations set up as follows:
1. JDK 1.8 (used to compile the basic library)
2. JDK   9 (used to compile the module descriptor)
3. JDK  13 (used to generate the JavaDoc)

These JDKs must be made visible to the build process by setting up
environment variables (or [Gradle properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties))
for each JDK version as follows:

```
JAVA_HOME="path to JDK 1.8"
JDK_8="path to JDK 1.8"
JDK_9="path to JDK 9"
JDK_13="path to JDK 13"
```

To build the native components for a platform, a compatible host is required.
Additionally, it is recommended to run the task with the `no-daemon` flag to
ensure that Gradle properly picks up all tools from the current environment.


### Building

Once the setup is complete, invoke the respective Gradle tasks using the
following command on Unix/macOS:

    ./gradlew <tasks>

or the following command on Windows:

    gradlew <tasks>

Important Gradle tasks to remember are:
- `clean`                   - clean build results
- `build`                   - assemble and test the Java library
- `buildNative<Platform>`   - assemble and test the platform-specific native
                              code
- `publishToMavenLocal`     - build and install all public artifacts to the
                              local maven repository

Additionally `tasks` may be used to print a list of all available tasks.


## License

```
Copyright (c) 2019-2020 Leon Linhart

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

For further information please refer to [LICENSE](LICENSE) and
[THIRDPARTY](./docs/THIRDPARTY).