### 2.0.2

_Released 2021 Mar 09_

#### Fixes

- Errors in native code no longer could crash the JVM.
- Errors in native code now throw `IllegalStateException` (as documented) instead of `NoClassDefFoundError`.