/*
 * Copyright (c) 2019-2024 Leon Linhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.gw2tb.gw2ml;

import org.jspecify.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.gw2tb.gw2ml.Native.*;
import static java.lang.foreign.MemoryLayout.PathElement.*;
import static java.lang.foreign.MemoryLayout.*;
import static java.lang.foreign.MemorySegment.*;
import static java.lang.foreign.ValueLayout.*;

/**
 * A {@link MumbleLink} object serves as a view for the data provided by a Guild Wars 2 game client in MumbleLink
 * format. The data may either be provided by the game client via the MumbleLink mechanism or by a custom source. An
 * instance for the former can be obtained by calling {@link #open()} - the primary entry point of GW2ML.
 *
 * @see <a href="https://wiki.mumble.info/wiki/Link">Mumble Wiki</a>
 * @see <a href="https://wiki.guildwars2.com/wiki/API:MumbleLink">Guild Wars 2 Wiki</a>
 *
 * @since   0.1.0
 *
 * @author  Leon Linhart
 */
public final class MumbleLink implements AutoCloseable {

    private static final String DEFAULT_HANDLE = "MumbleLink";

    private static final int AF_INET    = 2,
//                             AF_INET6   = (Platform.get() == Platform.WINDOWS) ? 23 : 10;
                             AF_INET6   = 23;

    /*
     * AF_INET:
     * struct sockaddr_in {
     *     sa_family_t    sin_family;   // AF_INET                      <-- ONLY 16bit on Windows for some reason...
     *     in_port_t      sin_port;     // port in network byte order
     *     struct in_addr sin_addr;     // internet address
     * }
     *
     * struct in_addr {
     *     uint32_t       s_addr;       // address in network byte order
     * }
     *
     * AF_INET6:
     * struct sockaddr_in6 {
     *      sa_family_t     sin6_family;    // AF_INET6                 <-- ONLY 16bit on Windows for some reason...
     *      in_port_t       sin6_port;      // port number
     *      uint32_t        sin6_flowinfo;  // IPv6 flow information
     *      struct in6_addr sin6_addr;      // IPv6 address
     *      uint32_t        sin6_scope_id;  // Scope ID (new in 2.4)
     * }
     *
     * struct in6_addr {
     *     unsigned char   s6_addr[16];     // IPv6 address
     * }
     */
    private static final MemoryLayout SOCKADDR_IN =
        unionLayout(
            structLayout(
                JAVA_SHORT.withName("ss_family"),
                JAVA_SHORT.withOrder(ByteOrder.BIG_ENDIAN).withName("sin_port"),
                sequenceLayout(4, JAVA_BYTE).withName("sin_addr")
            ).withName("sockaddr_in"),
            structLayout(
                JAVA_SHORT.withName("ss_family"),
                JAVA_SHORT.withOrder(ByteOrder.BIG_ENDIAN).withName("sin6_port"),
                JAVA_INT.withOrder(ByteOrder.BIG_ENDIAN).withName("sin6_flowinfo"),
                sequenceLayout(16, JAVA_BYTE).withName("sin6_addr"),
                JAVA_INT.withOrder(ByteOrder.BIG_ENDIAN).withName("sin6_scope_id")
            ).withName("sockaddr_in6")
    );

    private static final VarHandle VH_ss_family = SOCKADDR_IN.varHandle(groupElement("sockaddr_in"), groupElement("ss_family")).withInvokeExactBehavior();
    private static final VarHandle VH_sin_port = SOCKADDR_IN.varHandle(groupElement("sockaddr_in"), groupElement("sin_port")).withInvokeExactBehavior();
    private static final VarHandle VH_sin_addr = SOCKADDR_IN.varHandle(groupElement("sockaddr_in"), groupElement("sin_addr"), sequenceElement()).withInvokeExactBehavior();
    private static final VarHandle VH_sin6_port = SOCKADDR_IN.varHandle(groupElement("sockaddr_in6"), groupElement("sin6_port")).withInvokeExactBehavior();
    private static final VarHandle VH_sin6_addr = SOCKADDR_IN.varHandle(groupElement("sockaddr_in6"), groupElement("sin6_addr"), sequenceElement()).withInvokeExactBehavior();
    private static final VarHandle VH_sin6_scope_id = SOCKADDR_IN.varHandle(groupElement("sockaddr_in6"), groupElement("sin6_scope_id")).withInvokeExactBehavior();

    /*
     * struct LinkedMem {                               OFFSET      # ELEMENTS      # BYTES
     *     uint32_t uiVersion;                               0               1            4
     *     uint32_t uiTick;                                  4               1            4
     *     float fAvatarPosition[3];                         8               3           12
     *     float fAvatarFront[3];                           20               3           12
     *     float fAvatarTop[3];                             32               3           12
     *     wchar_t name[256];                               44             256          512
     *     float fCameraPosition[3];                       556               3           12
     *     float fCameraFront[3];                          568               3           12
     *     float fCameraTop[3];                            580               3           12
     *     wchar_t identity[256];                          592             256          512
     *     uint32_t context_len;                          1104               1            4
     *     unsigned char context[256] {                   1108             256          256
     *          unsigned char serverAddress[28];          1108+0            28           28
     *          uint32_t mapId;                           1108+28            1            4
     *          uint32_t mapType;                         1108+32            1            4
     *          uint32_t shardId;                         1108+36            1            4
     *          uint32_t instance;                        1108+40            1            4
     *          uint32_t buildId;                         1108+44            1            4
     *          uint32_t uiState;                         1108+48            1            4
     *          uint16_t compassWidth;                    1108+52            1            2
     *          uint16_t compassHeight;                   1108+54            1            2
     *          float compassRotation;                    1108+56            1            4
     *          float playerX;                            1108+60            1            4
     *          float playerY;                            1108+64            1            4
     *          float mapCenterX;                         1108+68            1            4
     *          float mapCenterY;                         1108+72            1            4
     *          float mapScale;                           1108+76            1            4
     *          uint32_t processId;                       1108+80            1            4
     *          uint8_t mountType;                        1108+84            1            1
     *     }
     *     wchar_t description[2048];                     1364            2048         4096
     * }
     *
     * TOTAL BYTES: 5460
     */
    private static final MemoryLayout LINKED_MEMORY = structLayout(
        JAVA_INT.withName("ui_version"),
        JAVA_INT.withName("ui_tick"),
        sequenceLayout(3, JAVA_FLOAT).withName("fAvatarPosition"),
        sequenceLayout(3, JAVA_FLOAT).withName("fAvatarFront"),
        sequenceLayout(3, JAVA_FLOAT).withName("fAvatarTop"),
        sequenceLayout(512, JAVA_BYTE).withName("name"),
        sequenceLayout(3, JAVA_FLOAT).withName("fCameraPosition"),
        sequenceLayout(3, JAVA_FLOAT).withName("fCameraFront"),
        sequenceLayout(3, JAVA_FLOAT).withName("fCameraTop"),
        sequenceLayout(512, JAVA_BYTE).withName("identity"),
        JAVA_INT.withName("context_len"),
        structLayout(
            sequenceLayout(28, JAVA_BYTE).withName("serverAddress"),
            JAVA_INT.withName("mapId"),
            JAVA_INT.withName("mapType"),
            JAVA_INT.withName("shardId"),
            JAVA_INT.withName("instance"),
            JAVA_INT.withName("buildId"),
            JAVA_INT.withName("uiState"),
            JAVA_SHORT.withName("compassWidth"),
            JAVA_SHORT.withName("compassHeight"),
            JAVA_FLOAT.withName("compassRotation"),
            JAVA_FLOAT.withName("playerX"),
            JAVA_FLOAT.withName("playerY"),
            JAVA_FLOAT.withName("mapCenterX"),
            JAVA_FLOAT.withName("mapCenterY"),
            JAVA_FLOAT.withName("mapScale"),
            JAVA_INT.withName("processId"),
            JAVA_BYTE.withName("mountType"),
            paddingLayout(171) // pad context up to 256
        ).withName("context"),
        sequenceLayout(4096, JAVA_BYTE).withName("description")
    );

    private static final VarHandle VH_uiVersion = LINKED_MEMORY.varHandle(groupElement("ui_version")).withInvokeExactBehavior();
    private static final VarHandle VH_uiTick = LINKED_MEMORY.varHandle(groupElement("ui_tick")).withInvokeExactBehavior();
    private static final VarHandle VH_fAvatarPosition = LINKED_MEMORY.varHandle(groupElement("fAvatarPosition"), sequenceElement()).withInvokeExactBehavior();
    private static final VarHandle VH_fAvatarFront = LINKED_MEMORY.varHandle(groupElement("fAvatarFront"), sequenceElement()).withInvokeExactBehavior();
    private static final VarHandle VH_fAvatarTop = LINKED_MEMORY.varHandle(groupElement("fAvatarTop"), sequenceElement()).withInvokeExactBehavior();
//    private static final VarHandle VH_name = LINKED_MEMORY.varHandle(groupElement("name"), sequenceElement()).withInvokeExactBehavior();
    private static final VarHandle VH_fCameraPosition = LINKED_MEMORY.varHandle(groupElement("fCameraPosition"), sequenceElement()).withInvokeExactBehavior();
    private static final VarHandle VH_fCameraFront = LINKED_MEMORY.varHandle(groupElement("fCameraFront"), sequenceElement()).withInvokeExactBehavior();
    private static final VarHandle VH_fCameraTop = LINKED_MEMORY.varHandle(groupElement("fCameraTop"), sequenceElement()).withInvokeExactBehavior();
    //    private static final VarHandle VH_context_identity = LINKED_MEMORY.varHandle(groupElement("identity"), sequenceElement()).withInvokeExactBehavior();
    private static final VarHandle VH_context_len = LINKED_MEMORY.varHandle(groupElement("context_len")).withInvokeExactBehavior();
    private static final VarHandle VH_context_serverAddress = LINKED_MEMORY.varHandle(groupElement("context"), groupElement("serverAddress"), sequenceElement()).withInvokeExactBehavior();
    private static final VarHandle VH_context_mapId = LINKED_MEMORY.varHandle(groupElement("context"), groupElement("mapId")).withInvokeExactBehavior();
    private static final VarHandle VH_context_mapType = LINKED_MEMORY.varHandle(groupElement("context"), groupElement("mapType")).withInvokeExactBehavior();
    private static final VarHandle VH_context_shardId = LINKED_MEMORY.varHandle(groupElement("context"), groupElement("shardId")).withInvokeExactBehavior();
    private static final VarHandle VH_context_instance = LINKED_MEMORY.varHandle(groupElement("context"), groupElement("instance")).withInvokeExactBehavior();
    private static final VarHandle VH_context_buildId = LINKED_MEMORY.varHandle(groupElement("context"), groupElement("buildId")).withInvokeExactBehavior();
    private static final VarHandle VH_context_uiState = LINKED_MEMORY.varHandle(groupElement("context"), groupElement("uiState")).withInvokeExactBehavior();
    private static final VarHandle VH_context_compassWidth = LINKED_MEMORY.varHandle(groupElement("context"), groupElement("compassWidth")).withInvokeExactBehavior();
    private static final VarHandle VH_context_compassHeight = LINKED_MEMORY.varHandle(groupElement("context"), groupElement("compassHeight")).withInvokeExactBehavior();
    private static final VarHandle VH_context_compassRotation = LINKED_MEMORY.varHandle(groupElement("context"), groupElement("compassRotation")).withInvokeExactBehavior();
    private static final VarHandle VH_context_playerX = LINKED_MEMORY.varHandle(groupElement("context"), groupElement("playerX")).withInvokeExactBehavior();
    private static final VarHandle VH_context_playerY = LINKED_MEMORY.varHandle(groupElement("context"), groupElement("playerY")).withInvokeExactBehavior();
    private static final VarHandle VH_context_mapCenterX = LINKED_MEMORY.varHandle(groupElement("context"), groupElement("mapCenterX")).withInvokeExactBehavior();
    private static final VarHandle VH_context_mapCenterY = LINKED_MEMORY.varHandle(groupElement("context"), groupElement("mapCenterY")).withInvokeExactBehavior();
    private static final VarHandle VH_context_mapScale = LINKED_MEMORY.varHandle(groupElement("context"), groupElement("mapScale")).withInvokeExactBehavior();
    private static final VarHandle VH_context_processId = LINKED_MEMORY.varHandle(groupElement("context"), groupElement("processId")).withInvokeExactBehavior();
    private static final VarHandle VH_context_mountType = LINKED_MEMORY.varHandle(groupElement("context"), groupElement("mountType")).withInvokeExactBehavior();
//    private static final VarHandle VH_description = LINKED_MEMORY.varHandle(groupElement("description"), sequenceElement()).withInvokeExactBehavior();

    /**
     * Opens a {@link MumbleLink} view of the data provided by Guild Wars 2 via the MumbleLink mechanism.
     *
     * <p>This method is shorthand for {@code MumbleLink.open("MumbleLink");}</p>
     *
     * @return  a {@code MumbleLink} object that may be used to read the data provided by Guild Wars 2 via the
     *          MumbleLink mechanism
     *
     * @throws IllegalStateException    if an unexpected error occurs
     *
     * @implNote    For better performance, this implementation reuses native resources whenever possible. In practice,
     *              this means that closing a MumbleLink object might not immediately close the underlying native
     *              resources.
     *
     * @since   0.1.0
     */
    public static MumbleLink open() {
        return open(DEFAULT_HANDLE);
    }

    /**
     * Opens a {@link MumbleLink} view of the data provided by Guild Wars 2 via the MumbleLink mechanism using a custom
     * handle.
     *
     * <p>The object returned by this method must be explicitly {@link #close() closed}.</p>
     *
     * <p>It is recommended to open a {@code MumbleLink} object once and keep it around for the lifetime of the
     * application when possible.</p>
     *
     * @param handle    the handle of the shared memory which will back the view
     *
     * @return  a {@code MumbleLink} object that may be used to read the data provided by Guild Wars 2 via the
     *          MumbleLink mechanism
     *
     * @throws IllegalStateException    if an unexpected error occurs
     *
     * @since   1.4.0
     */
    public static MumbleLink open(String handle) {
        MemorySegment hFileMapping = OpenFileMapping(FILE_MAP_WRITE, false, handle);

        if (hFileMapping.equals(NULL)) {
            hFileMapping = CreateFileMapping(INVALID_HANDLE_VALUE, NULL, PAGE_EXECUTE_READWRITE, 0, BYTES, handle);

            if (hFileMapping.equals(NULL)) {
                // TODO throw
                throw new IllegalStateException("Failed to create file mapping");
            }
        }

        MemorySegment linkedMemory = MapViewOfFile(hFileMapping, FILE_MAP_WRITE, 0, 0, BYTES);
        if (linkedMemory.equals(NULL)) {
            CloseHandle(hFileMapping);
            throw new IllegalStateException("Failed to close file mapping");
        }

        final MemorySegment _hFileMapping = hFileMapping;

        Arena arena = Arena.ofShared();
        MemorySegment data = linkedMemory.reinterpret(BYTES, arena, segment -> {
            UnmapViewOfFile(segment);
            CloseHandle(_hFileMapping);
        });

        return new MumbleLink(data, arena);
    }

    /**
     * Returns a {@link MumbleLink} view of the given buffer.
     *
     * <p>The buffer must be a {@link ByteBuffer#isDirect() direct} buffer.</p>
     *
     * @param buffer    the buffer to provide a {@link MumbleLink} view of
     *
     * @return  a {@code MumbleLink} object that may be used to read the data provided by the given buffer in the
     *          MumbleLink format
     *
     * @throws IllegalArgumentException if the given buffer is not direct
     *
     * @deprecated  This method is deprecated in favor of {@link #viewOf(MemorySegment)}.
     *
     * @since   1.3.0
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static MumbleLink viewOf(ByteBuffer buffer) {
        if (!buffer.isDirect()) throw new IllegalArgumentException("ByteBuffer must be direct");
        return new MumbleLink(MemorySegment.ofBuffer(buffer), null);
    }

    /**
     * Returns a {@link MumbleLink} view of the given memory segment.
     *
     * <p>The segment must be aligned.</p>
     *
     * @param segment   the segment to provide a {@link MumbleLink} view of
     *
     * @return  a {@code MumbleLink} object that may be used to read the data provided by the given segment in the
     *          MumbleLink format
     *
     * @throws IllegalArgumentException if the given segment is not aligned
     *
     * @since   3.0.0
     */
    public static MumbleLink viewOf(MemorySegment segment) {
        return new MumbleLink(segment, null);
    }

    private final Context context = new Context();

    private final MemorySegment data;
    private final boolean isCustom;
    private @Nullable Arena arena;

    private MumbleLink(MemorySegment data, @Nullable Arena arena) {
        this.data = data;
        this.isCustom = (arena == null);
        this.arena = arena;
    }

    /**
     * Clears the underlying buffer by setting all bytes to zero.
     *
     * @apiNote It may be useful to clear a {@code MumbleLink} instance when launching multiple Guild Wars 2 processes
     *          with the same mumble handle sequentially.
     *
     * @since   2.2.0
     */
    public void clear() {
        this.validateState();
        this.data.fill((byte) 0);
    }

    /**
     * Closes this resource.
     *
     * <p>This method does nothing if this view is backed by a custom buffer or if it has already been closed.</p>
     *
     * @since   0.1.0
     */
    @Override
    public void close() {
        if (this.arena == null) return;

        this.arena.close();
        this.arena = null;
    }

    /**
     * {@return whether this object is invalid}
     *
     * @see #close()
     *
     * @since   0.1.0
     */
    public boolean isClosed() {
        return (!this.isCustom && this.arena == null);
    }

    private void validateState() {
        if (this.isClosed()) throw new IllegalStateException("This view of the MumbleLink data is no longer valid.");
    }

    private static String wcharsToString(MemorySegment data, long offset, int length) {
        byte[] array = new byte[length * 2];
        int strLength = 0;
        boolean isValueAtLastEvenIndexZero = false;

        for (int i = 0; i < array.length; i++) {
            array[i] = data.get(JAVA_BYTE, offset + i);

            if (i % 2 == 0) {
                isValueAtLastEvenIndexZero = array[i] == 0;
            } else if (isValueAtLastEvenIndexZero) {
                strLength = i - 1;
                break;
            }
        }

        return strLength > 0 ? new String(array, 0, strLength, StandardCharsets.UTF_16LE) : "";
    }

    /**
     * The size of the MumbleLink buffer in bytes.
     *
     * @since   0.1.0
     */
    public static final int BYTES = (int) LINKED_MEMORY.byteSize() /* = 5460 */;

    /**
     * Shorthand for {@code copy(0, dest, 0, BYTES)}.
     *
     * <p>See {@link #copy(int, byte[], int, int)}.</p>
     *
     * @param dest  the destination array
     *
     * @throws IllegalStateException        if this view was {@link #isClosed() invalidated}
     * @throws IndexOutOfBoundsException    if any index is violated
     * @throws NullPointerException         if {@code dest} is {@code null}
     *
     * @since   1.3.0
     */
    public void copy(byte[] dest) {
        this.copy(0, dest, 0, BYTES);
    }

    /**
     * Shorthand for {@code copy(0, dest, 0, BYTES)}.
     *
     * <p>See {@link #copy(int, ByteBuffer, int, int)}.</p>
     *
     * @param dest  the destination buffer
     *
     * @throws IllegalStateException        if this view was {@link #isClosed() invalidated}
     * @throws IndexOutOfBoundsException    if any index is violated
     * @throws NullPointerException         if {@code dest} is {@code null}
     *
     * @deprecated  This method is deprecated in favor of {@link #copy(MemorySegment)}.
     *
     * @since   1.3.0
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public void copy(ByteBuffer dest) {
        this.copy(0, dest, 0, BYTES);
    }

    /**
     * Shorthand for {@code copy(0, dest, 0, BYTES)}.
     *
     * <p>See {@link #copy(int, MemorySegment, int, int)}.</p>
     *
     * @param dest  the destination segment
     *
     * @throws IllegalStateException        if this view was {@link #isClosed() invalidated}
     * @throws IndexOutOfBoundsException    if any index is violated
     * @throws NullPointerException         if {@code dest} is {@code null}
     *
     * @since   3.0.0
     */
    public void copy(MemorySegment dest) {
        this.copy(0, dest, 0, BYTES);
    }

    /**
     * Copies the underlying data beginning at the specified offset, to the specified offset of the destination
     * array.
     *
     * <p>If any of the following is true, an {@linkplain IndexOutOfBoundsException} is thrown and the destination
     * is not modified:</p>
     *
     * <ul>
     * <li>The {@code srcOffset} argument is negative.</li>
     * <li>The {@code destOffset} argument is negative.</li>
     * <li>The {@code length} argument is negative.</li>
     * <li>{@code srcOffset + length} is greater than {@link #BYTES}, the length the MumbleLink buffer</li>
     * <li>{@code destOffset + length} is greater than {@code dest.length}, the length of the destination array.</li>
     * </ul>
     *
     * @param srcOffset     starting position in the MumbleLink buffer
     * @param dest          the destination array
     * @param destOffset    starting position in the destination data
     * @param length        the number of bytes to be copied
     *
     * @throws IllegalStateException        if this view was {@link #isClosed() invalidated}
     * @throws IndexOutOfBoundsException    if any index is violated
     * @throws NullPointerException         if {@code dest} is {@code null}
     *
     * @since   1.3.0
     */
    public void copy(int srcOffset, byte[] dest, int destOffset, int length) {
        this.validateState();
        Objects.requireNonNull(dest);

        if (srcOffset < 0) throw new IndexOutOfBoundsException("srcOffset must be non-negative");
        if (destOffset < 0) throw new IndexOutOfBoundsException("destOffset must be non-negative");
        if (srcOffset + length > BYTES) throw new IndexOutOfBoundsException();
        if (destOffset + length > dest.length) throw new IndexOutOfBoundsException();

        for (int i = 0; i < length; i++) {
            dest[destOffset + i] = this.data.get(JAVA_BYTE, i);
        }
    }

    /**
     * Copies the underlying data beginning at the specified offset, to the specified offset of the destination
     * buffer.
     *
     * <p>If any of the following is true, an {@linkplain IndexOutOfBoundsException} is thrown and the destination
     * is not modified:</p>
     *
     * <ul>
     * <li>The {@code srcOffset} argument is negative.</li>
     * <li>The {@code destOffset} argument is negative.</li>
     * <li>The {@code length} argument is negative.</li>
     * <li>{@code srcOffset + length} is greater than {@link #BYTES}, the length the MumbleLink buffer</li>
     * <li>{@code destOffset + length} is greater than {@code dest.length}, the length of the destination buffer.</li>
     * </ul>
     *
     * @param srcOffset     starting position in the MumbleLink buffer
     * @param dest          the destination buffer
     * @param destOffset    starting position in the destination data
     * @param length        the number of bytes to be copied
     *
     * @throws IllegalStateException        if this view was {@link #isClosed() invalidated}
     * @throws IndexOutOfBoundsException    if any index is violated
     * @throws NullPointerException         if {@code dest} is {@code null}
     *
     * @deprecated  This method is deprecated in favor of {@link #copy(int, MemorySegment, int, int)}.
     *
     * @since   1.3.0
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public void copy(int srcOffset, ByteBuffer dest, int destOffset, int length) {
        this.validateState();
        Objects.requireNonNull(dest);

        if (srcOffset < 0) throw new IndexOutOfBoundsException("srcOffset must be non-negative");
        if (destOffset < 0) throw new IndexOutOfBoundsException("destOffset must be non-negative");
        if (srcOffset + length > BYTES) throw new IndexOutOfBoundsException();
        if (destOffset + length > dest.capacity()) throw new IndexOutOfBoundsException();

        for (int i = 0; i < length; i++) {
            dest.put(destOffset + i, this.data.get(JAVA_BYTE, srcOffset + i));
        }
    }

    /**
     * Copies the underlying data beginning at the specified offset, to the specified offset of the destination
     * buffer.
     *
     * <p>If any of the following is true, an {@linkplain IndexOutOfBoundsException} is thrown and the destination
     * is not modified:</p>
     *
     * <ul>
     * <li>The {@code srcOffset} argument is negative.</li>
     * <li>The {@code destOffset} argument is negative.</li>
     * <li>The {@code length} argument is negative.</li>
     * <li>{@code srcOffset + length} is greater than {@link #BYTES}, the length the MumbleLink buffer</li>
     * <li>{@code destOffset + length} is greater than {@code dest.length}, the length of the destination buffer.</li>
     * </ul>
     *
     * @param srcOffset     starting position in the MumbleLink buffer
     * @param dest          the destination buffer
     * @param destOffset    starting position in the destination data
     * @param length        the number of bytes to be copied
     *
     * @throws IllegalStateException        if this view was {@link #isClosed() invalidated}
     * @throws IndexOutOfBoundsException    if any index is violated
     * @throws NullPointerException         if {@code dest} is {@code null}
     *
     * @since   3.0.0
     */
    public void copy(int srcOffset, MemorySegment dest, int destOffset, int length) {
        this.validateState();
        Objects.requireNonNull(dest);

        if (srcOffset < 0) throw new IndexOutOfBoundsException("srcOffset must be non-negative");
        if (destOffset < 0) throw new IndexOutOfBoundsException("destOffset must be non-negative");
        if (srcOffset + length > BYTES) throw new IndexOutOfBoundsException();
        if (destOffset + length > dest.byteSize()) throw new IndexOutOfBoundsException();

        for (int i = 0; i < length; i++) {
            dest.set(JAVA_BYTE, destOffset + i, this.data.get(JAVA_BYTE, srcOffset + i));
        }
    }

    /**
     * {@return the version number as specified by the MumbleLink standard}
     *
     * <p>This is part of the MumbleLink standard and useless to most applications.</p>
     *
     * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
     *
     * @since   0.1.0
     */
    public long getUIVersion() {
        this.validateState();
        return Integer.toUnsignedLong((int) VH_uiVersion.get(this.data, 0L));
    }

    /**
     * {@return an integral identifier that is incremented every time the MumbleLink data is updated}
     *
     * <p>Notes:</p>
     *
     * <ul>
     * <li>At the time of writing MumbleLink data is not updated during loading screens. Thus, this identifier may be
     * used to (roughly) detect whether game client is currently in one. (Keep in mind that is behavior might change.)</li>
     * </ul>
     *
     * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
     *
     * @since   0.1.0
     */
    public long getUITick() {
        this.validateState();
        return Integer.toUnsignedLong((int) VH_uiTick.get(this.data, 0L));
    }

    /**
     * Returns the position of the avatar (in meters).
     *
     * <p>Notes:</p>
     *
     * <ul>
     * <li>Guild Wars 2's units correspond to inches.</li>
     * <li>The coordinate system used by MumbleLink is left-handed.</li>
     * <li>Due to limitations of the MumbleLink mechanism, it is possible that the underlying data is modified while
     * reading, thus the returned position may be incorrect. (In practice, this is fairly rare and can just be ignored
     * for the most part. Implement basic interpolation, if necessary.)</li>
     * </ul>
     *
     * @param dest  the array to store the data in
     *
     * @return  the {@code dest} array
     *
     * @throws IllegalArgumentException if {@code dest.length != 3}
     * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
     *
     * @since   0.1.0
     */
    public float[] getAvatarPosition(float[] dest) {
        if (dest.length != 3) throw new IllegalArgumentException();

        this.validateState();
        for (int i = 0; i < 3; i++) dest[i] = (float) VH_fAvatarPosition.get(this.data, 0L, (long) i);

        return dest;
    }

    /**
     * Returns the unit vector pointing out of the avatar's eyes.
     *
     * <p>Notes:</p>
     *
     * <ul>
     * <li>This is commonly referred to as "look-at-vector" in computer graphics.</li>
     * </ul>
     *
     * @param dest  the array to store the data in
     *
     * @return  the {@code dest} array
     *
     * @throws IllegalArgumentException if {@code dest.length != 3}
     * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
     *
     * @since   0.1.0
     */
    public float[] getAvatarFront(float[] dest) {
        if (dest.length != 3) throw new IllegalArgumentException();

        this.validateState();
        for (int i = 0; i < 3; i++) dest[i] = (float) VH_fAvatarFront.get(this.data, 0L, (long) i);

        return dest;
    }

    /**
     * Returns the unit vector pointing out of the top of the avatar's head.
     *
     * <p>Notes:</p>
     *
     * <ul>
     * <li>This is commonly referred to as "up-vector" in computer graphics.</li>
     * </ul>
     *
     * @param dest  the array to store the data in
     *
     * @return  the {@code dest} array
     *
     * @throws IllegalArgumentException if {@code dest.length != 3}
     * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
     *
     * @since   0.1.0
     */
    public float[] getAvatarTop(float[] dest) {
        if (dest.length != 3) throw new IllegalArgumentException();

        this.validateState();
        for (int i = 0; i < 3; i++) dest[i] = (float) VH_fAvatarTop.get(this.data, 0L, (long) i);

        return dest;
    }

    /**
     * Returns the name of the application which updated the underlying data last.
     *
     * @return  the name of the application which updated the underlying data last
     *
     * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
     *
     * @apiNote In practice this should always be {@code "Guild Wars 2"} when the game client has focus and is the owner
     *          of the link data. It is common practice to use this value to check which application is currently
     *          writing to the MumbleLink data.
     *
     * @since   0.1.0
     */
    public String getName() {
        this.validateState();
        return wcharsToString(this.data, LINKED_MEMORY.byteOffset(groupElement("name")), 256);
    }

    /**
     * Returns the position of the camera (in meters).
     *
     * <p>Notes:</p>
     *
     * <ul>
     * <li>Guild Wars 2's units correspond to inches.</li>
     * <li>The coordinate system used by MumbleLink is left-handed.</li>
     * <li>Due to limitations of the MumbleLink mechanism, it is possible that the underlying data is modified while
     * reading, thus the returned position may be incorrect. (In practice, this is fairly rare and can just be ignored
     * for the most part. Implement basic interpolation, if necessary.)</li>
     * </ul>
     *
     * @param dest  the array to store the data in
     *
     * @return  the {@code dest} array
     *
     * @throws IllegalArgumentException if {@code dest.length != 3}
     * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
     *
     * @since   0.1.0
     */
    public float[] getCameraPosition(float[] dest) {
        if (dest.length != 3) throw new IllegalArgumentException();

        this.validateState();
        for (int i = 0; i < 3; i++) dest[i] = (float) VH_fCameraPosition.get(this.data, 0L, (long) i);

        return dest;
    }

    /**
     * Returns the unit vector pointing out of the camera.
     *
     * <p>Notes:</p>
     *
     * <ul>
     * <li>This is commonly referred to as "look-at-vector" in computer graphics.</li>
     * </ul>
     *
     * @param dest  the array to store the data in
     *
     * @return  the {@code dest} array
     *
     * @throws IllegalArgumentException if {@code dest.length != 3}
     * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
     *
     * @since   0.1.0
     */
    public float[] getCameraFront(float[] dest) {
        if (dest.length != 3) throw new IllegalArgumentException();

        this.validateState();
        for (int i = 0; i < 3; i++) dest[i] = (float) VH_fCameraFront.get(this.data, 0L, (long) i);

        return dest;
    }

    /**
     * Returns the unit vector pointing out of the top of the camera.
     *
     * <p>Notes:</p>
     *
     * <ul>
     * <li>This is commonly referred to as "up-vector" in computer graphics.</li>
     * </ul>
     *
     * @param dest  the array to store the data in
     *
     * @return  the {@code dest} array
     *
     * @throws IllegalArgumentException if {@code dest.length != 3}
     * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
     *
     * @since   0.1.0
     */
    public float[] getCameraTop(float[] dest) {
        if (dest.length != 3) throw new IllegalArgumentException();

        this.validateState();
        for (int i = 0; i < 3; i++) dest[i] = (float) VH_fCameraTop.get(this.data, 0L, (long) i);

        return dest;
    }

    /**
     * {@return a JSON-formatted {@code String} with additional information}
     *
     * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
     *
     * @since   0.1.0
     */
    public String getIdentity() {
        this.validateState();
        return wcharsToString(this.data, LINKED_MEMORY.byteOffset(groupElement("identity")), 1024);
    }

    /**
     * {@return the length of the context (in bytes) that will be used by a MumbleServer to determine whether two users
     * can hear each other positionally}
     *
     * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
     *
     * @since   0.1.0
     */
    public long getContextLength() {
        this.validateState();
        return Integer.toUnsignedLong((int) VH_context_len.get(this.data, 0L));
    }

    /**
     * {@return a {@code Context} object that may be used to access the additional context information}
     *
     * <p>The returned object is strongly tied to this object and is only valid as long as this object is valid.</p>
     *
     * @since   0.1.0
     */
    public Context getContext() {
        return this.context;
    }

    /**
     * {@return the description that may provide additional information about the game's current state}
     *
     * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
     *
     * @apiNote This field is currently not used by the game client.
     *
     * @since   0.1.0
     */
    public String getDescription() {
        this.validateState();
        return wcharsToString(this.data, LINKED_MEMORY.byteOffset(groupElement("description")), 4096);
    }

    /**
     * See {@link #getContext()}.
     *
     * @since   0.1.0
     */
    public final class Context {

        /**
         * The number of bytes that are used to store the context in the underlying data.
         *
         * @since   0.1.0
         */
        public static final int BYTES = 256;

        private Context() {}

        /**
         * Shorthand for {@code copy(0, dest, 0, BYTES)}.
         *
         * <p>See {@link #copy(int, byte[], int, int)}.</p>
         *
         * @param dest  the destination array
         *
         * @throws IllegalStateException        if this view was {@link #isClosed() invalidated}
         * @throws IndexOutOfBoundsException    if any index is violated
         * @throws NullPointerException         if {@code dest} is {@code null}
         *
         * @since   0.1.0
         */
        public void copy(byte[] dest) {
            this.copy(0, dest, 0, BYTES);
        }

        /**
         * Shorthand for {@code copy(0, dest, 0, BYTES)}.
         *
         * <p>See {@link #copy(int, ByteBuffer, int, int)}.</p>
         *
         * @param dest  the destination buffer
         *
         * @throws IllegalStateException        if this view was {@link #isClosed() invalidated}
         * @throws IndexOutOfBoundsException    if any index is violated
         * @throws NullPointerException         if {@code dest} is {@code null}
         *
         * @deprecated  This method is deprecated in favor of {@link #copy(MemorySegment)}.
         *
         * @since   1.3.0
         */
        @Deprecated(since = "3.0.0", forRemoval = true)
        public void copy(ByteBuffer dest) {
            this.copy(0, dest, 0, BYTES);
        }

        /**
         * Shorthand for {@code copy(0, dest, 0, BYTES)}.
         *
         * <p>See {@link #copy(int, MemorySegment, int, int)}.</p>
         *
         * @param dest  the destination segment
         *
         * @throws IllegalStateException        if this view was {@link #isClosed() invalidated}
         * @throws IndexOutOfBoundsException    if any index is violated
         * @throws NullPointerException         if {@code dest} is {@code null}
         *
         * @since   3.0.0
         */
        public void copy(MemorySegment dest) {
            this.copy(0, dest, 0, BYTES);
        }

        /**
         * Copies the underlying data beginning at the specified offset, to the specified offset of the destination
         * array.
         *
         * <p>If any of the following is true, an {@linkplain IndexOutOfBoundsException} is thrown and the destination
         * is not modified:</p>
         *
         * <ul>
         * <li>The {@code srcOffset} argument is negative.</li>
         * <li>The {@code destOffset} argument is negative.</li>
         * <li>The {@code length} argument is negative.</li>
         * <li>{@code srcOffset + length} is greater than {@link #BYTES}, the length of the context</li>
         * <li>{@code destOffset + length} is greater than {@code dest.length}, the length of the destination array.</li>
         * </ul>
         *
         * @param srcOffset     starting position in the context
         * @param dest          the destination array
         * @param destOffset    starting position in the destination data
         * @param length        the number of bytes to be copied
         *
         * @throws IllegalStateException        if this view was {@link #isClosed() invalidated}
         * @throws IndexOutOfBoundsException    if any index is violated
         * @throws NullPointerException         if {@code dest} is {@code null}
         *
         * @since   0.1.0
         */
        public void copy(int srcOffset, byte[] dest, int destOffset, int length) {
            MumbleLink.this.validateState();
            Objects.requireNonNull(dest);

            if (srcOffset < 0) throw new IndexOutOfBoundsException("srcOffset must be non-negative");
            if (destOffset < 0) throw new IndexOutOfBoundsException("destOffset must be non-negative");
            if (srcOffset + length > BYTES) throw new IndexOutOfBoundsException();
            if (destOffset + length > dest.length) throw new IndexOutOfBoundsException();

            for (int i = 0; i < length; i++) {
                dest[destOffset + i] = MumbleLink.this.data.get(JAVA_BYTE, LINKED_MEMORY.byteOffset(groupElement("context")) + srcOffset + i);
            }
        }

        /**
         * Copies the underlying data beginning at the specified offset, to the specified offset of the destination
         * buffer.
         *
         * <p>If any of the following is true, an {@linkplain IndexOutOfBoundsException} is thrown and the destination
         * is not modified:</p>
         *
         * <ul>
         * <li>The {@code srcOffset} argument is negative.</li>
         * <li>The {@code destOffset} argument is negative.</li>
         * <li>The {@code length} argument is negative.</li>
         * <li>{@code srcOffset + length} is greater than {@link #BYTES}, the length of the context</li>
         * <li>{@code destOffset + length} is greater than {@code dest.length}, the length of the destination buffer.</li>
         * </ul>
         *
         * @param srcOffset     starting position in the context
         * @param dest          the destination buffer
         * @param destOffset    starting position in the destination data
         * @param length        the number of bytes to be copied
         *
         * @throws IllegalStateException        if this view was {@link #isClosed() invalidated}
         * @throws IndexOutOfBoundsException    if any index is violated
         * @throws NullPointerException         if {@code dest} is {@code null}
         *
         * @deprecated  This method is deprecated in favor of {@link #copy(int, MemorySegment, int, int)}.
         *
         * @since   1.3.0
         */
        @Deprecated(since = "3.0.0", forRemoval = true)
        public void copy(int srcOffset, ByteBuffer dest, int destOffset, int length) {
            MumbleLink.this.validateState();
            Objects.requireNonNull(dest);

            if (srcOffset < 0) throw new IndexOutOfBoundsException("srcOffset must be non-negative");
            if (destOffset < 0) throw new IndexOutOfBoundsException("destOffset must be non-negative");
            if (srcOffset + length > BYTES) throw new IndexOutOfBoundsException();
            if (destOffset + length > dest.capacity()) throw new IndexOutOfBoundsException();

            for (int i = 0; i < length; i++) {
                dest.put(destOffset + i, MumbleLink.this.data.get(JAVA_BYTE, LINKED_MEMORY.byteOffset(groupElement("context")) + srcOffset + i));
            }
        }

        /**
         * Copies the underlying data beginning at the specified offset, to the specified offset of the destination
         * segment.
         *
         * <p>If any of the following is true, an {@linkplain IndexOutOfBoundsException} is thrown and the destination
         * is not modified:</p>
         *
         * <ul>
         * <li>The {@code srcOffset} argument is negative.</li>
         * <li>The {@code destOffset} argument is negative.</li>
         * <li>The {@code length} argument is negative.</li>
         * <li>{@code srcOffset + length} is greater than {@link #BYTES}, the length of the context</li>
         * <li>{@code destOffset + length} is greater than {@code dest.length}, the length of the destination buffer.</li>
         * </ul>
         *
         * @param srcOffset     starting position in the context
         * @param dest          the destination segment
         * @param destOffset    starting position in the destination data
         * @param length        the number of bytes to be copied
         *
         * @throws IllegalStateException        if this view was {@link #isClosed() invalidated}
         * @throws IndexOutOfBoundsException    if any index is violated
         * @throws NullPointerException         if {@code dest} is {@code null}
         *
         * @since   3.0.0
         */
        public void copy(int srcOffset, MemorySegment dest, int destOffset, int length) {
            MumbleLink.this.validateState();
            Objects.requireNonNull(dest);

            if (srcOffset < 0) throw new IndexOutOfBoundsException("srcOffset must be non-negative");
            if (destOffset < 0) throw new IndexOutOfBoundsException("destOffset must be non-negative");
            if (srcOffset + length > BYTES) throw new IndexOutOfBoundsException();
            if (destOffset + length > dest.byteSize()) throw new IndexOutOfBoundsException();

            for (int i = 0; i < length; i++) {
                dest.set(JAVA_BYTE, destOffset + i, MumbleLink.this.data.get(JAVA_BYTE, LINKED_MEMORY.byteOffset(groupElement("context")) + srcOffset + i));
            }
        }

        @SuppressWarnings("resource")
        private final MemorySegment serverAddress = Arena.ofAuto().allocate(28, 4);

        @Nullable
        private InetSocketAddress inetAddress;

        /**
         * {@return the address of the map server that the game client is currently connected to}
         *
         * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
         *
         * @since   0.1.0
         */
        @Nullable
        public InetSocketAddress getServerAddress() {
            MumbleLink.this.validateState();

            boolean isInvalid = (this.inetAddress == null);

            for (long i = 0; i < SOCKADDR_IN.byteSize(); i++) {
                byte b = (byte) VH_context_serverAddress.get(MumbleLink.this.data, 0L, i);
                isInvalid |= (this.serverAddress.get(JAVA_BYTE, i) != b);
                this.serverAddress.set(JAVA_BYTE, i, b);
            }

            if (isInvalid) {
                int family = Short.toUnsignedInt((short) VH_ss_family.get(this.serverAddress, 0L));

                if (family == AF_INET) {
                    int port = Short.toUnsignedInt((short) VH_sin_port.get(this.serverAddress, 0L));

                    byte[] addr = new byte[4];
                    for (int i = 0; i < addr.length; i++) addr[i] = (byte) VH_sin_addr.get(this.serverAddress, 0L, (long) i);

                    InetAddress inetAddress;

                    try {
                        inetAddress = InetAddress.getByAddress(addr);
                    } catch (UnknownHostException e) {
                        throw new RuntimeException("Failed to parse IPv4 server address", e);
                    }

                    this.inetAddress = new InetSocketAddress(inetAddress, port);
                } else if (family == AF_INET6) {
                    int port = Short.toUnsignedInt((short) VH_sin6_port.get(this.serverAddress, 0L));
                    // TODO flow information is currently ignored (but should not be required)

                    byte[] addr = new byte[16];
                    for (int i = 0; i < addr.length; i++) addr[i] = (byte) VH_sin6_addr.get(this.serverAddress, 0L, (long) i);

                    int scopeId = (int) VH_sin6_scope_id.get(this.serverAddress, 0L);
                    InetAddress inetAddress;

                    try {
                        inetAddress = Inet6Address.getByAddress(null, addr, scopeId);
                    } catch (UnknownHostException e) {
                        throw new RuntimeException("Failed to parse IPv6 server address", e);
                    }

                    this.inetAddress = new InetSocketAddress(inetAddress, port);
                } else if (family != 0) {
                    throw new RuntimeException("Unknown server address family: " + family);
                } else {
                    this.inetAddress = null;
                }
            }

            return this.inetAddress;
        }

        /**
         * Returns the ID of the map the player is currently on (as per
         * <a href="https://wiki.guildwars2.com/wiki/API:2/maps">Guild Wars 2 API</a>).
         *
         * @return  the ID of the map the player is currently on
         *
         * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
         *
         * @since   0.1.0
         */
        public long getMapID() {
            MumbleLink.this.validateState();
            return Integer.toUnsignedLong((int) VH_context_mapId.get(MumbleLink.this.data, 0L));
        }

        /**
         * {@return information about the type of the map the player is currently on}
         *
         * <p>The functionality provided by {@link MapType} may be used to interpret the value.</p>
         *
         * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
         *
         * @since   0.1.0
         */
        public long getMapType() {
            MumbleLink.this.validateState();
            return Integer.toUnsignedLong((int) VH_context_mapType.get(MumbleLink.this.data, 0L));
        }

        /**
         * {@return a 32bit bitfield that contains various information about the current game shard}
         *
         * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
         *
         * @apiNote This field has no known purpose for outside use.
         *
         * @since   1.0.0
         */
        public int getShardID() {
            MumbleLink.this.validateState();
            return (int) VH_context_shardId.get(MumbleLink.this.data, 0L);
        }

        /**
         * {@return the ID of the current game instance}
         *
         * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
         *
         * @apiNote This field has no known purpose for outside use.
         *
         * @since   0.1.0
         */
        public long getInstance() {
            MumbleLink.this.validateState();
            return Integer.toUnsignedLong((int) VH_context_instance.get(MumbleLink.this.data, 0L));
        }

        /**
         * Returns the ID of the game build that is currently running (as per
         * <a href="https://wiki.guildwars2.com/wiki/API:2/build">Guild Wars 2 API</a>).
         *
         * @return  the ID of the game build that is currently running
         *
         * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
         *
         * @since   0.1.0
         */
        public long getBuildID() {
            MumbleLink.this.validateState();
            return Integer.toUnsignedLong((int) VH_context_buildId.get(MumbleLink.this.data, 0L));
        }

        /**
         * {@return a 32bit bitfield that contains various information about the current state of the game UI}
         *
         * <p>The functionality provided by {@link UIState} may be used to interpret the value of the bitfield.</p>
         *
         * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
         *
         * @since   0.1.0
         */
        public int getUIState() {
            MumbleLink.this.validateState();
            return (int) VH_context_uiState.get(MumbleLink.this.data, 0L);
        }

        /**
         * {@return the width of the compass in pixels}
         *
         * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
         *
         * @since   0.1.0
         */
        public int getCompassWidth() {
            MumbleLink.this.validateState();
            return Short.toUnsignedInt((short) VH_context_compassWidth.get(MumbleLink.this.data, 0L));
        }

        /**
         * {@return the height of the compass in pixels}
         *
         * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
         *
         * @since   0.1.0
         */
        public int getCompassHeight() {
            MumbleLink.this.validateState();
            return Short.toUnsignedInt((short) VH_context_compassHeight.get(MumbleLink.this.data, 0L));
        }

        /**
         * {@return the rotation of the compass in radians}
         *
         * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
         *
         * @since   0.1.0
         */
        public float getCompassRotation() {
            MumbleLink.this.validateState();
            return (float) VH_context_compassRotation.get(MumbleLink.this.data, 0L);
        }

        /**
         * {@return the {@code X}-component of the position of the player in continent coordinates}
         *
         * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
         *
         * @since   0.1.0
         */
        public float getPlayerX() {
            MumbleLink.this.validateState();
            return (float) VH_context_playerX.get(MumbleLink.this.data, 0L);
        }

        /**
         * {@return the {@code Y}-component of the position of the player in continent coordinates}
         *
         * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
         *
         * @since   0.1.0
         */
        public float getPlayerY() {
            MumbleLink.this.validateState();
            return (float) VH_context_playerY.get(MumbleLink.this.data, 0L);
        }

        /**
         * {@return the {@code X}-component of the position at the center of the map}
         *
         * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
         *
         * @since   0.1.0
         */
        public float getMapCenterX() {
            MumbleLink.this.validateState();
            return (float) VH_context_mapCenterX.get(MumbleLink.this.data, 0L);
        }

        /**
         * {@return the {@code Y}-component of the position at the center of the map}
         *
         * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
         *
         * @since   0.1.0
         */
        public float getMapCenterY() {
            MumbleLink.this.validateState();
            return (float) VH_context_mapCenterY.get(MumbleLink.this.data, 0L);
        }

        /**
         * {@return the scale of the map}
         *
         * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
         *
         * @since   0.1.0
         */
        public float getMapScale() {
            MumbleLink.this.validateState();
            return (float) VH_context_mapScale.get(MumbleLink.this.data, 0L);
        }

        /**
         * {@return the ID of the process}
         *
         * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
         *
         * @since   1.4.0
         */
        public long getProcessID() {
            MumbleLink.this.validateState();
            return Integer.toUnsignedLong((int) VH_context_processId.get(MumbleLink.this.data, 0L));
        }

        /**
         * {@return information about the type of the mount the player is currently riding}
         *
         * <p>The functionality provided by {@link MountType} may be used to interpret the value.</p>
         *
         * @throws IllegalStateException    if this view was {@link #isClosed() invalidated}
         *
         * @since   1.5.0
         */
        public byte getMountType() {
            MumbleLink.this.validateState();
            return (byte) VH_context_mountType.get(MumbleLink.this.data, 0L);
        }

    }

}