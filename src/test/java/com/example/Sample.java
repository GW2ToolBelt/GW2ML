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
package com.example;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import com.github.gw2toolbelt.gw2ml.MapType;
import com.github.gw2toolbelt.gw2ml.MumbleLink;
import com.github.gw2toolbelt.gw2ml.UIState;

public class Sample {

    public static void main(String[] args) {
        try (MumbleLink mumbleLink = MumbleLink.open()) {
            while (true) {
                float[] buffer = new float[3];

                System.out.println("=== GW2ML Sample ===");
                System.out.println(String.format("uiVersion:\t\t\t\t%s", mumbleLink.getUIVersion()));
                System.out.println(String.format("uiTick:\t\t\t\t\t%s", mumbleLink.getUITick()));
                System.out.println(String.format("fAvatarPosition:\t\t%s", Arrays.toString(mumbleLink.getAvatarPosition(buffer))));
                System.out.println(String.format("fAvatarFront:\t\t\t%s", Arrays.toString(mumbleLink.getAvatarFront(buffer))));
                System.out.println(String.format("fAvatarTop:\t\t\t\t%s", Arrays.toString(mumbleLink.getAvatarTop(buffer))));
                System.out.println(String.format("name:\t\t\t\t\t%s", mumbleLink.getName()));
                System.out.println(String.format("fCameraPosition:\t\t%s", Arrays.toString(mumbleLink.getCameraPosition(buffer))));
                System.out.println(String.format("fCameraFront:\t\t\t%s", Arrays.toString(mumbleLink.getCameraFront(buffer))));
                System.out.println(String.format("fCameraTop:\t\t\t\t%s", Arrays.toString(mumbleLink.getCameraTop(buffer))));
                System.out.println(String.format("identity:\t\t\t\t%s", mumbleLink.getIdentity()));
                System.out.println(String.format("contextLength:\t\t\t%s", mumbleLink.getContextLength()));
                System.out.println(String.format("ctx_ServerAddress:\t\t%s", mumbleLink.getContext().getServerAddress()));
                System.out.println(String.format("ctx_MapID:\t\t\t\t%s", mumbleLink.getContext().getMapID()));

                long mapType = mumbleLink.getContext().getMapType();
                System.out.println(String.format(
                    "ctx_MapType:\t\t\t%s (%s)",
                    mapType, MapType.valueOf(mapType).toString())
                );

                System.out.println(String.format("ctx_ShardID:\t\t\t%s", Integer.toBinaryString(mumbleLink.getContext().getShardID())));
                System.out.println(String.format("ctx_Instance:\t\t\t%s", mumbleLink.getContext().getInstance()));
                System.out.println(String.format("ctx_BuildID:\t\t\t%s", mumbleLink.getContext().getBuildID()));

                int uiState = mumbleLink.getContext().getUIState();
                System.out.println(String.format(
                    "ctx_UIState:\t\t\t%s (isMapOpen=%s, isCompassTopRight=%s, isCompassRotationEnabled=%s, isGameFocused=%s, isInCompetitiveMode=%s, isTextFieldFocused=%s, isInCombat=%s)",
                    Integer.toBinaryString(uiState),
                    UIState.isMapOpen(uiState),
                    UIState.isCompassTopRight(uiState),
                    UIState.isCompassRotationEnabled(uiState),
                    UIState.isGameFocused(uiState),
                    UIState.isInCompetitiveMode(uiState),
                    UIState.isTextFieldFocused(uiState),
                    UIState.isInCombat(uiState)
                ));

                System.out.println(String.format("ctx_CompassWidth:\t\t%s", mumbleLink.getContext().getCompassWidth()));
                System.out.println(String.format("ctx_CompassHeight:\t\t%s", mumbleLink.getContext().getCompassHeight()));
                System.out.println(String.format("ctx_CompassRotation:\t%s", mumbleLink.getContext().getCompassRotation()));
                System.out.println(String.format("ctx_PlayerX:\t\t\t%s", mumbleLink.getContext().getPlayerX()));
                System.out.println(String.format("ctx_PlayerY:\t\t\t%s", mumbleLink.getContext().getPlayerY()));
                System.out.println(String.format("ctx_MapCenterX:\t\t\t%s", mumbleLink.getContext().getMapCenterX()));
                System.out.println(String.format("ctx_MapCenterY:\t\t\t%s", mumbleLink.getContext().getMapCenterY()));
                System.out.println(String.format("ctx_MapScale:\t\t\t%s", mumbleLink.getContext().getMapScale()));
                System.out.println(String.format("description:\t\t\t%s", mumbleLink.getDescription()));
                System.out.println();

                sleepAtLeast(5, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * Sleeps for at least the given amount of time.
     *
     * @param timeout   the timeout
     * @param unit      the unit of the timeout
     *
     * @throws IllegalArgumentException if the given {@code unit} is unsupported
     */
    public static void sleepAtLeast(long timeout, TimeUnit unit) {
        if (unit == TimeUnit.NANOSECONDS || unit == TimeUnit.MICROSECONDS) throw new IllegalArgumentException();

        long startTime = System.currentTimeMillis();
        long endTime = startTime + unit.toMillis(timeout);
        long sleepBudget = endTime - startTime;

        while (sleepBudget > 0) {
            try {
                unit.sleep(timeout);
            } catch (InterruptedException e) {}

            sleepBudget -= System.currentTimeMillis() - startTime;
        }
    }

}