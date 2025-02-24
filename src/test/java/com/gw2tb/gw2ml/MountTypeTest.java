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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MountType}.
 *
 * @author  Leon Linhart
 */
public final class MountTypeTest {

    @Test
    public void testNumericalValue() {
        assertEquals(0, MountType.NONE.numericalValue());
        assertEquals(1, MountType.JACKAL.numericalValue());
        assertEquals(2, MountType.GRIFFON.numericalValue());
        assertEquals(3, MountType.SPRINGER.numericalValue());
        assertEquals(4, MountType.SKIMMER.numericalValue());
        assertEquals(5, MountType.RAPTOR.numericalValue());
        assertEquals(6, MountType.ROLLERBEETLE.numericalValue());
        assertEquals(7, MountType.WARCLAW.numericalValue());
        assertEquals(8, MountType.SKYSCALE.numericalValue());
        assertEquals(9, MountType.SKIFF.numericalValue());
        assertEquals(10, MountType.SIEGETURTLE.numericalValue());

        assertThrows(IllegalArgumentException.class, MountType.UNKNOWN::numericalValue);
    }

    @Test
    public void testValueOf() {
        assertEquals(MountType.NONE, MountType.valueOf(0));
        assertEquals(MountType.JACKAL, MountType.valueOf(1));
        assertEquals(MountType.GRIFFON, MountType.valueOf(2));
        assertEquals(MountType.SPRINGER, MountType.valueOf(3));
        assertEquals(MountType.SKIMMER, MountType.valueOf(4));
        assertEquals(MountType.RAPTOR, MountType.valueOf(5));
        assertEquals(MountType.ROLLERBEETLE, MountType.valueOf(6));
        assertEquals(MountType.WARCLAW, MountType.valueOf(7));
        assertEquals(MountType.SKYSCALE, MountType.valueOf(8));
        assertEquals(MountType.SKIFF, MountType.valueOf(9));
        assertEquals(MountType.SIEGETURTLE, MountType.valueOf(10));

        assertEquals(MountType.UNKNOWN, MountType.valueOf(11));
        assertEquals(MountType.UNKNOWN, MountType.valueOf(-1));
    }

}