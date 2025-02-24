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
 * Unit tests for {@link MapType}.
 *
 * @author  Leon Linhart
 */
public final class MapTypeTest {

    @Test
    public void testNumericalValue() {
        assertEquals(0, MapType.REDIRECT.numericalValue());
        assertEquals(1, MapType.CHARACTER_CREATION.numericalValue());
        assertEquals(2, MapType.PVP.numericalValue());
        assertEquals(3, MapType.GVG.numericalValue());
        assertEquals(4, MapType.INSTANCE.numericalValue());
        assertEquals(5, MapType.PUBLIC.numericalValue());
        assertEquals(6, MapType.TOURNAMENT.numericalValue());
        assertEquals(7, MapType.TUTORIAL.numericalValue());
        assertEquals(8, MapType.USER_TOURNAMENT.numericalValue());
        assertEquals(9, MapType.ETERNAL_BATTLEGROUNDS.numericalValue());
        assertEquals(10, MapType.BLUE_BORDERLANDS.numericalValue());
        assertEquals(11, MapType.GREEN_BORDERLANDS.numericalValue());
        assertEquals(12, MapType.RED_BORDERLANDS.numericalValue());
        assertEquals(13, MapType.FORTUNES_VALE.numericalValue());
        assertEquals(14, MapType.OBSIDIAN_SANCTUM.numericalValue());
        assertEquals(15, MapType.EOTM.numericalValue());
        assertEquals(16, MapType.PUBLIC_MINI.numericalValue());
        assertEquals(17, MapType.BIG_BATTLE.numericalValue());
        assertEquals(18, MapType.WVW_LOUNGE.numericalValue());
        assertEquals(19, MapType.WVW.numericalValue());

        assertThrows(IllegalArgumentException.class, MapType.UNKNOWN::numericalValue);
    }

    @Test
    public void testValueOf() {
        assertEquals(MapType.REDIRECT, MapType.valueOf(0));
        assertEquals(MapType.CHARACTER_CREATION, MapType.valueOf(1));
        assertEquals(MapType.PVP, MapType.valueOf(2));
        assertEquals(MapType.GVG, MapType.valueOf(3));
        assertEquals(MapType.INSTANCE, MapType.valueOf(4));
        assertEquals(MapType.PUBLIC, MapType.valueOf(5));
        assertEquals(MapType.TOURNAMENT, MapType.valueOf(6));
        assertEquals(MapType.TUTORIAL, MapType.valueOf(7));
        assertEquals(MapType.USER_TOURNAMENT, MapType.valueOf(8));
        assertEquals(MapType.ETERNAL_BATTLEGROUNDS, MapType.valueOf(9));
        assertEquals(MapType.BLUE_BORDERLANDS, MapType.valueOf(10));
        assertEquals(MapType.GREEN_BORDERLANDS, MapType.valueOf(11));
        assertEquals(MapType.RED_BORDERLANDS, MapType.valueOf(12));
        assertEquals(MapType.FORTUNES_VALE, MapType.valueOf(13));
        assertEquals(MapType.OBSIDIAN_SANCTUM, MapType.valueOf(14));
        assertEquals(MapType.EOTM, MapType.valueOf(15));
        assertEquals(MapType.PUBLIC_MINI, MapType.valueOf(16));
        assertEquals(MapType.BIG_BATTLE, MapType.valueOf(17));
        assertEquals(MapType.WVW_LOUNGE, MapType.valueOf(18));
        assertEquals(MapType.WVW, MapType.valueOf(19));

        assertEquals(MapType.UNKNOWN, MapType.valueOf(20));
        assertEquals(MapType.UNKNOWN, MapType.valueOf(-1));
    }

}