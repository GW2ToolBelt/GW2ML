### 3.1.0

_Released 2025 Aug 12_

#### Improvements

- JSpecify is now exposed as "api" dependency to consumers.
    - This is done to ensure that tooling can properly handle the
      JSpecify-annotated public API of this library.


---

### 3.0.2

_Released 2025 Feb 24_

#### Fixes

- The library no longer depends on JUnit at runtime.


---

### 3.0.1

_Released 2024 Apr 16_

#### Fixes

- `MumbleLink#clear` does no longer crash the JVM. [[GH-25](https://github.com/GW2ToolBelt/GW2ML/issues/25)]


---

### 3.0.0

_Released 2024 Apr 13_

#### Improvements

- Migrated from JNI to Java's new FFI.
  - This removes the need to have a separate native artifact alongside the
    library.
  - Introduced new method overloads that accept `MemorySegment` instead of
    `ByteBuffer`.
  - Deprecated the `ByteBuffer` methods for removal.
- Migrated from JSR305 annotations to [JSpecify](https://jspecify.dev/).

#### Breaking Changes

- Attempts to receive the numerical value for `UNKNOWN` enum types will now
  throw an `IllegalArgumentException`.
- `MumbleLink#viewOf(ByteBuffer)` now requires a direct buffer.
- Removed (public) dependency on `com.google.code.findbugs:jsr305`.
- Removed the deprecated `MountType#valueOf(long)` overload.
- The deprecated public constructor of `MumbleLink.Context` has been hidden.
- The minimum required Java version is now 22.


---

### 2.2.2

_Released 2022 Aug 27_

#### Fixes

- Removed instance caching to resolve issues with closing of cached instances.
- The byte order is now properly reset after parsing the server address.


---

### 2.2.1

_Released 2022 Aug 25_

#### Fixes

- Clearing a `MumbleLink` instance that wraps a custom `ByteBuffer` does now
  properly zeroes the buffer's content.
- The server address is now parsed using network byte order.


---

### 2.2.0

_Released 2022 Jul 22_

#### Improvements

- Added support for clearing the MumbleLink buffer (via `MumbleLink#clear()`).

#### Fixes

- Several fixes and improvements to server address parsing. (Thanks to @Medyro)
  - The port is now parsed correctly instead of just the first byte.
  - IPv6 addresses are now properly recognized on Windows.
  - Improved error messages for parsing failures.


---

### 2.1.2

_Released 2022 May 10_

#### Fixes

- Fixed an `IndexOutOfBoundsException` caused by server address caching.


---

### 2.1.1

_Released 2022 Apr 30_

#### Fixes

- Improved runtime library extraction.


---

### 2.1.0

_Released 2022 Feb 05_

#### Improvements

- Added `MountType` constants for the Skiff and the Siege Turtle.
- Added caching for `MumbleLink.Context#getServerAddress()`.
- Improved lookup speed for `MapType#valueOf` and `MountType#valueOf`.

#### Deprecations

- Deprecated `MapType#valueOf(long)`.


---

### 2.0.3

_Released 2021 Nov 03_

#### Improvements

- Added version information to the module descriptor.


---

### 2.0.2

_Released 2021 Mar 09_

#### Fixes

- Errors in native code no longer could crash the JVM.
- Errors in native code now throw `IllegalStateException` (as documented) instead of `NoClassDefFoundError`.


---

### 2.0.1

_Released 2021 Feb 27_

#### Overview

This is a maintenance release only and does not contain any behavioral change.


---

### 2.0.0

_Released 2020 Sep 03_

#### Overview

With `2.0.0` comes a major maintenance release that removes previously
deprecated functionality and - most importantly - migrates to the `gw2tb.com`
domain to bring GW2ML in line with upcoming libraries.

#### Breaking Changes

- Switched from the "gw2toolbelt.github.com" URL to "gw2tb.com". (This affects
  all package names, module names and imports.) 
- Removed the mislabeled `UIState#isInLoadingScreen`.


---

### 1.5.0

_Released 2020 May 27_

#### Improvements

- Added `MumbleLink.Context#getMountType` which returns the ID of the current
  mount and `MountType` which provides the functionality required to interpret
  such IDs.

#### Fixes

- Use the correct module descriptor for native binaries [[GH-15](https://github.com/GW2Toolbelt/GW2ML/issues/15)].


---

### 1.4.0

_Released 2020 Apr 28_

#### Improvements

- Added support for specifying file handle explicitly when opening a view via
  `MumbleLink#open(String)`.
    - This is meant to be used with the `-mumble <name>` command line argument
      that was added to Guild Wars 2 on 28. April 2020 to better support
      multiple game clients on a single PC.
- Added `UIState#isInCombat` bit check.
- Added `MumbleLink.Context#getProcessID` which returns the ID of the game
  process that has most recently updated the data.


---

### 1.3.0

_Released 2020 Apr 08_

#### Improvements

- Added support for MumbleLink views of custom source ByteBuffers.
- Added `MumbleLink#copy` operations to cover from the entire MumbleLink buffer (instead of just the context).
- Added ByteBuffer overloads for `MumbleLink.Context#copy` operations.

#### Fixes

- Fixes an issue that caused `MumbleLink.Context#copy` operations to copy from
  the start of the buffer instead of the start from the context.


---

### 1.2.0

_Released 2020 Mar 20_

#### Improvements

- Added `UIState#isTextFieldFocused` bit check [[GH-12](https://github.com/GW2Toolbelt/GW2ML/issues/12)].


---

### 1.1.0

_Released 2020 Feb 19_

#### Improvements

- Added `UIState#isGameFocused` and `UIState#isInCompetitiveMode`.
    - `UIState#isGameFocused` replaces the mislabeled `UIState#isInLoadingScreen`. [[GH-11](https://github.com/GW2Toolbelt/GW2ML/issues/11)]

#### Deprecations

- Deprecated the mislabeled `UIState#isInLoadingScreen`.


---

### 1.0.0

_Released 2020 Jan 08_

#### Improvements

- Throw descriptive exceptions for errors in native code. [[GH-10](https://github.com/GW2Toolbelt/GW2ML/issues/10)]
- Documented all remaining undocumented fields. [[GH-2](https://github.com/GW2Toolbelt/GW2ML/issues/2)]
  [[GH-3](https://github.com/GW2Toolbelt/GW2ML/issues/3)] [[GH-4](https://github.com/GW2Toolbelt/GW2ML/issues/4)]
  [[GH-5](https://github.com/GW2Toolbelt/GW2ML/issues/5)] [[GH-6](https://github.com/GW2Toolbelt/GW2ML/issues/6)]
  [[GH-7](https://github.com/GW2Toolbelt/GW2ML/issues/7)] [[GH-8](https://github.com/GW2Toolbelt/GW2ML/issues/8)]
- Added `UIState#isInLoadingScreen` to check whether the game is currently in a loading screen.

#### Breaking Changes

- Changed return type of `MumbleLink.Context#getShardID` from `long` to `int`


---

### 0.1.0

_Released 2019 Oct 31_

#### Overview

This is the first pre-release of GW2ML, a library for accessing data provided by
the Guild Wars 2 game client via the MumbleLink mechanism.

This is a initial version is published for easy public consumption and the API
might change in the future (although that is unlikely).