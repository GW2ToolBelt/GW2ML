/*
 * Copyright (c) 2019-2020 Leon Linhart
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
package com.github.gw2toolbelt.gw2ml;

/**
 * A utility class for interpreting the value of the {@link MumbleLink.Context#getUIState() uiState} bitfield.
 *
 * @since   0.1.0
 *
 * @author  Leon Linhart
 */
public final class UIState {

    /**
     * Returns whether or not the world map is currently open.
     *
     * @param uiState   the {@link MumbleLink.Context#getUIState() uiState} bitfield
     *
     * @return  whether or not the world map is currently open
     *
     * @since   0.1.0
     */
    public static boolean isMapOpen(int uiState) {
        //noinspection PointlessBitwiseExpression
        return (uiState & (1 << 0)) != 0;
    }

    /**
     * Returns whether or not the compass is in the upper right corner of the screen.
     *
     * <p>If the compass is not in the upper right corner of the screen, it is in the bottom right corner of the screen.
     * </p>
     *
     * @param uiState   the {@link MumbleLink.Context#getUIState() uiState} bitfield
     *
     * @return  whether or not the compass is in the upper right corner of the screen
     *
     * @since   0.1.0
     */
    public static boolean isCompassTopRight(int uiState) {
        return (uiState & (1 << 1)) != 0;
    }

    /**
     * Returns whether or not rotation is enabled for the ingame compass.
     *
     * @param uiState   the {@link MumbleLink.Context#getUIState() uiState} bitfield
     *
     * @return  whether or not rotation is enabled for the ingame compass
     *
     * @since   0.1.0
     */
    public static boolean isCompassRotationEnabled(int uiState) {
        return (uiState & (1 << 2)) != 0;
    }

    /**
     * Returns whether or not the game is currently in a loading screen.
     *
     * @param uiState   the {@link MumbleLink.Context#getUIState() uiState} bitfield
     *
     * @return  whether or not the game is currently in a loading screen
     *
     * @deprecated  This bit is wrongly labeled. Use {@link #isGameFocused(int)} instead.
     *
     * @since   1.0.0
     */
    @Deprecated
    public static boolean isInLoadingScreen(int uiState) {
        return (uiState & (1 << 3)) != 0;
    }

    /**
     * Returns whether or not the game client is currently focused.
     *
     * @param uiState   the {@link MumbleLink.Context#getUIState() uiState} bitfield
     *
     * @return  whether or not the game client is currently focused
     *
     * @since   1.1.0
     */
    public static boolean isGameFocused(int uiState) {
        return (uiState & (1 << 3)) != 0;
    }

    /**
     * Returns whether or not the player is currently in a competitive mode.
     *
     * @param uiState   the {@link MumbleLink.Context#getUIState() uiState} bitfield
     *
     * @return  whether or not the player is currently in a competitive mode
     *
     * @since   1.1.0
     */
    public static boolean isInCompetitiveMode(int uiState) {
        return (uiState & (1 << 4)) != 0;
    }

    // This utility class only provides static functionality and is not meant to be initialized.
    private UIState() {}

}