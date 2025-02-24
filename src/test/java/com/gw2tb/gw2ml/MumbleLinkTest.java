/*
 * Copyright (c) 2019-2025 Leon Linhart
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.lang.foreign.Arena;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

@EnabledOnOs(OS.WINDOWS)
public final class MumbleLinkTest {

    @Test
    public void testOpen() {
        try (var mumbleLink = MumbleLink.open()) {
            assertEquals(0, mumbleLink.getUIVersion());
        }
    }

    @Test
    public void testOpen_Named() {
        try (var mumbleLink = MumbleLink.open("wulululululu")) {
            assertEquals(0, mumbleLink.getUIVersion());
        }
    }

    @SuppressWarnings("removal")
    @Test
    public void testViewOf_ByteBuffer() {
        try (var mumbleLink = MumbleLink.viewOf(ByteBuffer.allocateDirect(MumbleLink.BYTES))) {
            assertEquals(0, mumbleLink.getUIVersion());
        }

        assertThrows(IllegalArgumentException.class, () -> MumbleLink.viewOf(ByteBuffer.allocate(MumbleLink.BYTES)));
    }

    @Test
    public void testViewOf_MemorySegment() {
        try (
            var arena = Arena.ofConfined();
            var mumbleLink = MumbleLink.viewOf(arena.allocate(MumbleLink.BYTES))
        ) {
            assertEquals(0, mumbleLink.getUIVersion());
        }
    }

    @Test
    public void testClear() {
        try (MumbleLink mumbleLink = MumbleLink.open()) {
            mumbleLink.clear();
        }
    }

    @Test
    public void testClose() {
        MumbleLink mumbleLink = MumbleLink.open();
        assertEquals(0, mumbleLink.getUIVersion());

        mumbleLink.close();
        assertThrows(IllegalStateException.class, mumbleLink::getUIVersion);
    }

}