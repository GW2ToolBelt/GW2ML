/*
 * Copyright (c) 2019-2022 Leon Linhart
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
import com.gw2tb.gw2ml.MapType;
import com.gw2tb.gw2ml.MountType;
import com.gw2tb.gw2ml.MumbleLink;
import com.gw2tb.gw2ml.UIState;

public class Sample {

    public static void main(String[] args) {
        try (MumbleLink mumbleLink = MumbleLink.open()) {
            while (true) {
                float[] buffer = new float[3];

                System.out.println("=== GW2ML Sample ===");
                System.out.printf("uiVersion:\t\t\t\t%s%n", mumbleLink.getUIVersion());
                System.out.printf("uiTick:\t\t\t\t\t%s%n", mumbleLink.getUITick());
                System.out.printf("fAvatarPosition:\t\t%s%n", Arrays.toString(mumbleLink.getAvatarPosition(buffer)));
                System.out.printf("fAvatarFront:\t\t\t%s%n", Arrays.toString(mumbleLink.getAvatarFront(buffer)));
                System.out.printf("fAvatarTop:\t\t\t\t%s%n", Arrays.toString(mumbleLink.getAvatarTop(buffer)));
                System.out.printf("name:\t\t\t\t\t%s%n", mumbleLink.getName());
                System.out.printf("fCameraPosition:\t\t%s%n", Arrays.toString(mumbleLink.getCameraPosition(buffer)));
                System.out.printf("fCameraFront:\t\t\t%s%n", Arrays.toString(mumbleLink.getCameraFront(buffer)));
                System.out.printf("fCameraTop:\t\t\t\t%s%n", Arrays.toString(mumbleLink.getCameraTop(buffer)));
                System.out.printf("identity:\t\t\t\t%s%n", mumbleLink.getIdentity());
                System.out.printf("contextLength:\t\t\t%s%n", mumbleLink.getContextLength());
                System.out.printf("ctx_ServerAddress:\t\t%s%n", mumbleLink.getContext().getServerAddress());
                System.out.printf("ctx_MapID:\t\t\t\t%s%n", mumbleLink.getContext().getMapID());

                long mapType = mumbleLink.getContext().getMapType();
                System.out.printf(
                    "ctx_MapType:\t\t\t%s (%s)%n",
                    mapType, MapType.valueOf(mapType)
                );

                System.out.printf("ctx_ShardID:\t\t\t%s%n", Integer.toBinaryString(mumbleLink.getContext().getShardID()));
                System.out.printf("ctx_Instance:\t\t\t%s%n", mumbleLink.getContext().getInstance());
                System.out.printf("ctx_BuildID:\t\t\t%s%n", mumbleLink.getContext().getBuildID());

                int uiState = mumbleLink.getContext().getUIState();
                System.out.printf(
                    "ctx_UIState:\t\t\t%s (isMapOpen=%s, isCompassTopRight=%s, isCompassRotationEnabled=%s, isGameFocused=%s, isInCompetitiveMode=%s, isTextFieldFocused=%s, isInCombat=%s)%n",
                    Integer.toBinaryString(uiState),
                    UIState.isMapOpen(uiState),
                    UIState.isCompassTopRight(uiState),
                    UIState.isCompassRotationEnabled(uiState),
                    UIState.isGameFocused(uiState),
                    UIState.isInCompetitiveMode(uiState),
                    UIState.isTextFieldFocused(uiState),
                    UIState.isInCombat(uiState)
                );

                System.out.printf("ctx_CompassWidth:\t\t%s%n", mumbleLink.getContext().getCompassWidth());
                System.out.printf("ctx_CompassHeight:\t\t%s%n", mumbleLink.getContext().getCompassHeight());
                System.out.printf("ctx_CompassRotation:\t%s%n", mumbleLink.getContext().getCompassRotation());
                System.out.printf("ctx_PlayerX:\t\t\t%s%n", mumbleLink.getContext().getPlayerX());
                System.out.printf("ctx_PlayerY:\t\t\t%s%n", mumbleLink.getContext().getPlayerY());
                System.out.printf("ctx_MapCenterX:\t\t\t%s%n", mumbleLink.getContext().getMapCenterX());
                System.out.printf("ctx_MapCenterY:\t\t\t%s%n", mumbleLink.getContext().getMapCenterY());
                System.out.printf("ctx_MapScale:\t\t\t%s%n", mumbleLink.getContext().getMapScale());
                System.out.printf("ctx_ProcessID:\t\t\t%s%n", mumbleLink.getContext().getProcessID());

                byte mountType = mumbleLink.getContext().getMountType();
                System.out.printf(
                    "ctx_MountType:\t\t\t%s (%s)%n",
                    mountType, MountType.valueOf(mountType)
                );

                System.out.printf("description:\t\t\t%s%n%n", mumbleLink.getDescription());

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
        long stopTime = startTime + unit.toMillis(timeout);
        long sleepBudget = stopTime - startTime;

        while (sleepBudget > 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(sleepBudget);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            sleepBudget = stopTime - System.currentTimeMillis();
        }
    }

}