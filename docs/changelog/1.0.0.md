### 1.0.0

_2020 Jan 08_

#### Improvements

- Throw descriptive exceptions for errors in native code. [[GH-10](https://github.com/GW2Toolbelt/GW2ML/issues/10)]
- Documented all remaining undocumented fields. [[GH-2](https://github.com/GW2Toolbelt/GW2ML/issues/2)]
  [[GH-3](https://github.com/GW2Toolbelt/GW2ML/issues/3)] [[GH-4](https://github.com/GW2Toolbelt/GW2ML/issues/4)]
  [[GH-5](https://github.com/GW2Toolbelt/GW2ML/issues/5)] [[GH-6](https://github.com/GW2Toolbelt/GW2ML/issues/6)]
  [[GH-7](https://github.com/GW2Toolbelt/GW2ML/issues/7)] [[GH-8](https://github.com/GW2Toolbelt/GW2ML/issues/8)]
- Added `UIState#isInLoadingScreen` to check whether the game is currently in a loading screen.

#### Breaking Changes

- Changed return type of `MumbleLink.Context#getShardID` from `long` to `int`