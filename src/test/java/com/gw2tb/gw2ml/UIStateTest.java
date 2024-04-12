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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UIState}
 *
 * @author  Leon Linhart
 */
public final class UIStateTest {

    @Test
    public void testIsMapOpen() {
        assertTrue(UIState.isMapOpen(0b00000000000000000000000000000001));
        assertTrue(UIState.isMapOpen(0b11111111111111111111111111111111));

        assertFalse(UIState.isMapOpen(0b11111111111111111111111111111110));
        assertFalse(UIState.isMapOpen(0b10101010101010101010101010101010));
    }

    @Test
    public void testIsCompassTopRight() {
        assertTrue(UIState.isCompassTopRight(0b00000000000000000000000000000010));
        assertTrue(UIState.isCompassTopRight(0b11111111111111111111111111111111));

        assertFalse(UIState.isCompassTopRight(0b11111111111111111111111111111101));
        assertFalse(UIState.isCompassTopRight(0b01010101010101010101010101010101));
    }

    @Test
    public void testIsCompassRotationEnabled() {
        assertTrue(UIState.isCompassRotationEnabled(0b00000000000000000000000000000100));
        assertTrue(UIState.isCompassRotationEnabled(0b11111111111111111111111111111111));

        assertFalse(UIState.isCompassRotationEnabled(0b11111111111111111111111111111011));
        assertFalse(UIState.isCompassRotationEnabled(0b10101010101010101010101010101010));
    }

    @Test
    public void testIsGameFocused() {
        assertTrue(UIState.isGameFocused(0b00000000000000000000000000001000));
        assertTrue(UIState.isGameFocused(0b11111111111111111111111111111111));

        assertFalse(UIState.isGameFocused(0b11111111111111111111111111110111));
        assertFalse(UIState.isGameFocused(0b01010101010101010101010101010101));
    }

    @Test
    public void testIsInCompetitiveMode() {
        assertTrue(UIState.isInCompetitiveMode(0b00000000000000000000000000010000));
        assertTrue(UIState.isInCompetitiveMode(0b11111111111111111111111111111111));

        assertFalse(UIState.isInCompetitiveMode(0b11111111111111111111111111101111));
        assertFalse(UIState.isInCompetitiveMode(0b10101010101010101010101010101010));
    }

    @Test
    public void testIsTextFieldFocused() {
        assertTrue(UIState.isTextFieldFocused(0b00000000000000000000000000100000));
        assertTrue(UIState.isTextFieldFocused(0b11111111111111111111111111111111));

        assertFalse(UIState.isTextFieldFocused(0b11111111111111111111111111011111));
        assertFalse(UIState.isTextFieldFocused(0b01010101010101010101010101010101));
    }

    @Test
    public void testIsInCombat() {
        assertTrue(UIState.isInCombat(0b00000000000000000000000001000000));
        assertTrue(UIState.isInCombat(0b11111111111111111111111111111111));

        assertFalse(UIState.isInCombat(0b11111111111111111111111110111111));
        assertFalse(UIState.isInCombat(0b10101010101010101010101010101010));
    }

}