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