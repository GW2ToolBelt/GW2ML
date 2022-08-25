### 2.2.1

_Released 2022 Aug 25_

#### Fixes

- Clearing a `MumbleLink` instance that wraps a custom `ByteBuffer` does now
  properly zeroes the buffer's content.
- The server address is now parsed using network byte order.