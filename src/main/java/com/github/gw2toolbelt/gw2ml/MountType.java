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

import java.util.Arrays;

/**
 * A utility class for interpreting the value of the {@link MumbleLink.Context#getMountType() mount type} field.
 *
 * @since   1.5.0
 *
 * @author  Leon Linhart
 */
public enum MountType {
    /**
     * Represents any unknown or unexpected {@code mountType} value.
     *
     * <p>This is used as a fallback.</p>
     *
     * @since   1.5.0
     */
    UNKNOWN(-1),
    /**
     * The player is not riding any mount.
     *
     * @since   1.5.0
     */
    NONE(0),
    /**
     * The "Jackal" mount.
     *
     * @since   1.5.0
     */
    JACKAL(1),
    /**
     * The "Griffon" mount.
     *
     * @since   1.5.0
     */
    GRIFFON(2),
    /**
     * The "Springer" mount.
     *
     * @since   1.5.0
     */
    SPRINGER(3),
    /**
     * The "Skimmer" mount.
     *
     * @since   1.5.0
     */
    SKIMMER(4),
    /**
     * The "Raptor" mount.
     *
     * @since   1.5.0
     */
    RAPTOR(5),
    /**
     * The "Roller Beetle" mount.
     *
     * @since   1.5.0
     */
    ROLLERBEETLE(6),
    /**
     * The "Warclaw" mount.
     *
     * @since   1.5.0
     */
    WARCLAW(7),
    /**
     * The "Skyscale" mount.
     *
     * @since   1.5.0
     */
    SKYSCALE(8);

    /**
     * Returns the appropriate {@code MountType} representation for the given numerical value.
     *
     * <p>If the given value does not correspond to any known {@code MountType}, {@link #UNKNOWN} is returned.</p>
     *
     * @param mountType   the map type to search
     *
     * @return  the appropriate {@code MountType} representation for the given numerical value
     *
     * @since   1.5.0
     */
    public static MountType valueOf(long mountType) {
        return Arrays.stream(MountType.values())
            .filter(it -> it.numericalValue() == mountType)
            .findFirst()
            .orElse(MountType.UNKNOWN);
    }

    private final byte mountType;

    MountType(int mountType) {
        this.mountType = (byte) mountType;
    }

    /**
     * Returns the numerical value of this mount type.
     *
     * <p>This is the same value as the value returned by {@link MumbleLink.Context#getMountType()}.</p>
     *
     * @return  the numerical value of this mount type
     *
     * @since   1.5.0
     */
    public long numericalValue() {
        return this.mountType;
    }

}