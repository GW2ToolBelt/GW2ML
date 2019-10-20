# GW2ML

GW2ML is a Java library for accessing the data provided by the Guild Wars 2
game client via the MumbleLink mechanism with as little overhead as possible.

The library is still supporting Java 8 and makes use of Java 9's multi-release
feature to fully support the module system.


**NOTE: While this library is fully functional its documentation is currently
still lackluster. [Read more about the current state of this library.](https://github.com/TheMrMilchmann/GW2ML/issues/9)**


## Usage

The library was designed to provide a simple API while remaining as efficient
as possible. The primary entry-point for the library is [`MumbleLink.open()](https://github.com/TheMrMilchmann/GW2ML/blob/master/src/main/java/com/github/gw2toolbelt/gw2ml/MumbleLink.java#L89)https://github.com/TheMrMilchmann/GW2ML/blob/master/src/main/java/com/github/gw2toolbelt/gw2ml/MumbleLink.java#L89).
Follow the documentation of that class if you want to learn more about how to
use GW2ML.

A basic can be found [here](https://github.com/TheMrMilchmann/GW2ML/blob/master/src/test/java/com/example/Sample.java).


## Building from source

Building GW2ML requires two JDK installations set up as follows:
1. One of `JDK8_HOME`, `JAVA8_HOME`, or `JDK_8` must point at a valid JDK8
   installation.
2. One of `JDK9_HOME`, `JAVA9_HOME`, or `JDK_9` must point at a valid
   installation of a JDK that support compiling to Java 9 bytecode.

Once the setup is complete, building GW2ML is as simple as invoking the desired
Gradle task. For example: In order to run a full build of the project, call:

        ./gradlew build

Compiling the native components of the library is currently only supported on
windows hosts. If you wish to build only the Java components please use the
`assemble` task instead.


## License

```
Copyright (c) 2019 Leon Linhart

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