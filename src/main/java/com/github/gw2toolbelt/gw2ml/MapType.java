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

/*
 * Full MapType enum as published on 08 Apr 2020 07:56 UTC.
 *
 * enum EMapType {
 *     MAP_TYPE_AUTO_REDIRECT,
 *     MAP_TYPE_CHAR_CREATE,
 *     MAP_TYPE_COMPETITIVE_PVP,
 *     MAP_TYPE_GVG,
 *     MAP_TYPE_INSTANCE,
 *     MAP_TYPE_PUBLIC,
 *     MAP_TYPE_TOURNAMENT,
 *     MAP_TYPE_TUTORIAL,
 *     MAP_TYPE_USERTOURN,
 *     MAP_TYPE_WVW_CENTER,
 *     MAP_TYPE_WVW_BLUE_HOME,
 *     MAP_TYPE_WVW_GREEN_HOME,
 *     MAP_TYPE_WVW_RED_HOME,
 *     MAP_TYPE_WVW_REWARD,
 *     MAP_TYPE_WVW_EB_JUMP_PUZZLE,
 *     MAP_TYPE_WVW_OVERFLOW,
 *     MAP_TYPE_PUBLIC_MINI,
 *     MAP_TYPE_BIG_BATTLE,        // NO LONGER SUPPORTED
 *     MAP_TYPE_WVW_LOUNGE,
 *     MAP_TYPE_WVW,
 *     MAP_TYPES
 * };
 */

/**
 * A utility class for interpreting the value of the {@link MumbleLink.Context#getMapType() mapType} field.
 *
 * @since   1.0.0
 *
 * @author  Leon Linhart
 */
public enum MapType {
    /**
     * Represents any unknown or unexpected {@code mapType} value.
     *
     * <p>This is used as a fallback.</p>
     *
     * @since   1.0.0
     */
    UNKNOWN(-1),
    /**
     * Redirect "maps" (e.g. when logging in while in a PvP match).
     *
     * @since   1.0.0
     */
    REDIRECT(0),
    /**
     * Character creation screens.
     *
     * @since   1.0.0
     */
    CHARACTER_CREATION(1),
    /**
     * Competitive Player vs Player (PvP) maps.
     *
     * @since   1.0.0
     */
    PVP(2),
    /**
     * Guild vs Guild (GvG) maps.
     *
     * @apiNote At the time of writing this `mapType` is unused.
     *
     * @since   1.0.0
     */
    GVG(3),
    /**
     * Instance maps (such as dungeons and story content).
     *
     * @since   1.0.0
     */
    INSTANCE(4),
    /**
     * Public maps (e.g. open world).
     *
     * @since   1.0.0
     */
    PUBLIC(5),
    /**
     * Tournament maps.
     *
     * @since   1.0.0
     */
    TOURNAMENT(6),
    /**
     * Tutorial maps.
     *
     * @since   1.0.0
     */
    TUTORIAL(7),
    /**
     * User tournament maps.
     *
     * @since   1.0.0
     */
    USER_TOURNAMENT(8),
    /**
     * "Eternal Battlegrounds" maps.
     *
     * @since   1.0.0
     */
    ETERNAL_BATTLEGROUNDS(9),
    /**
     * "Blue Battlegrounds" maps.
     *
     * @since   1.0.0
     */
    BLUE_BORDERLANDS(10),
    /**
     * "Green Battlegrounds" maps.
     *
     * @since   1.0.0
     */
    GREEN_BORDERLANDS(11),
    /**
     * "Red Battlegrounds" maps.
     *
     * @since   1.0.0
     */
    RED_BORDERLANDS(12),
    /**
     * <strong>This map type is currently unused and it's purpose is unknown.</strong>
     *
     * @since   1.0.0
     */
    FORTUNES_VALE(13),
    /**
     * "Obsidian Sanctum" maps.
     *
     * @since   1.0.0
     */
    OBSIDIAN_SANCTUM(14),
    /**
     * "Edge of the Mists" maps.
     *
     * @since   1.0.0
     */
    EOTM(15),
    /**
     * Public "mini" maps (such as "Dry Top", "The Silverwastes", and "Mistlock Sanctuary").
     *
     * @since   1.0.0
     */
    PUBLIC_MINI(16),
    /**
     * <strong>This map type is currently unused and it's purpose is unknown.</strong>
     *
     * @since   1.3.0
     */
    BIG_BATTLE(17),
    /**
     * "Armistice Bastion" maps.
     *
     * @apiNote This map type might be re-used by other WvW lounge maps in the
     *          future.
     *
     * @since   1.0.0
     */
    WVW_LOUNGE(18),
    /**
     * <strong>This map type's purpose is unknown.</strong>
     *
     * @since   1.3.0
     */
    WVW(19);

    /**
     * Returns the appropriate {@code MapType} representation for the given numerical value.
     *
     * <p>If the given value does not correspond to any known {@code MapType}, {@link #UNKNOWN} is returned.</p>
     *
     * @param mapType   the map type to search
     *
     * @return  the appropriate {@code MapType} representation for the given numerical value
     *
     * @since   1.0.0
     */
    public static MapType valueOf(long mapType) {
        return Arrays.stream(MapType.values())
            .filter(it -> it.numericalValue() == mapType)
            .findFirst()
            .orElse(MapType.UNKNOWN);
    }

    private final long mapType;

    MapType(long mapType) {
        this.mapType = mapType;
    }

    /**
     * Returns the numerical value of this map type.
     *
     * <p>This is the same value as the value returned by {@link MumbleLink.Context#getMapType()}.</p>
     *
     * @return  the numerical value of this map type
     *
     * @since   1.0.0
     */
    public long numericalValue() {
        return this.mapType;
    }

}