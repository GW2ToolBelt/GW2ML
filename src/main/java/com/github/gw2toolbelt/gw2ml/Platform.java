/*
 * This file is based on the corresponding implementation in LWJGL which is
 * distributed under the BSD-3 license as per original file header:
 *
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 *
 *
 * This modified version is distributed as part of GW2ML under the following
 * conditions:
 *
 * Copyright (c) 2019 Leon Linhart
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

import java.util.function.Function;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import static com.github.gw2toolbelt.gw2ml.JNILibraryLoader.*;

/**
 * Platform specific tools.
 *
 * @author  LWJGL
 */
enum Platform {
    LINUX("Linux", "linux") {

        private final Pattern SO = Pattern.compile("(?:^|/)lib\\w+[.]so(?:[.]\\d+)*$");

        @Override
        String mapLibraryName(String name) {
            if (SO.matcher(name).find()) return name;
            return System.mapLibraryName(name);
        }

    },
    MACOS("macOS", "macos") {

        private final Pattern DYLIB = Pattern.compile("(?:^|/)lib\\w+(?:[.]\\d+)*[.]dylib$");

        @Override
        String mapLibraryName(String name) {
            if (DYLIB.matcher(name).find()) return name;
            return System.mapLibraryName(name);
        }

    },
    WINDOWS("Windows", "windows") {

        @Override
        String mapLibraryName(String name) {
            if (name.endsWith(".dll")) return name;
            return System.mapLibraryName(name);
        }

    };

    /** The architectures supported by LWJGL. */
    public enum Architecture {
        X64(true),
        X86(false),
        ARM64(true),
        ARM32(false);

        static final Architecture current;

        final boolean is64Bit;

        static {
            String osArch  = System.getProperty("os.arch");
            boolean is64Bit = osArch.contains("64") || osArch.startsWith("armv8");

            current = osArch.startsWith("arm") || osArch.startsWith("aarch64")
                ? (is64Bit ? ARM64 : ARM32)
                : (is64Bit ? X64 : X86);
        }

        Architecture(boolean is64Bit) {
            this.is64Bit = is64Bit;
        }
    }

    private static final Platform current;

    private static final Function<String, String> bundledLibraryNameMapper;
    private static final Function<String, String> bundledLibraryPathMapper;

    static {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            current = WINDOWS;
        } else if (osName.startsWith("Linux") || osName.startsWith("FreeBSD") || osName.startsWith("SunOS") || osName.startsWith("Unix")) {
            current = LINUX;
        } else if (osName.startsWith("Mac OS X") || osName.startsWith("Darwin")) {
            current = MACOS;
        } else {
            throw new LinkageError("Unknown platform: " + osName);
        }

        bundledLibraryNameMapper = getMapper(
            Configuration.BUNDLED_LIBRARY_NAME_MAPPER.get("default"),
            name -> name,
            name -> Architecture.current.is64Bit ? name : name + "32"
        );
        bundledLibraryPathMapper = getMapper(
            Configuration.BUNDLED_LIBRARY_PATH_MAPPER.get("default"),
            name -> current.nativePath + "/" + Architecture.current.name().toLowerCase() + "/" + name,
            name -> name.substring(name.lastIndexOf('/'))
        );
    }

    private final String name;
    private final String nativePath;

    Platform(String name, String nativePath) {
        this.name = name;
        this.nativePath = nativePath;
    }

    /** Returns the platform name. */
    public String getName() {
        return name;
    }

    abstract String mapLibraryName(String name);

    /** Returns the platform on which the library is running. */
    public static Platform get() {
        return current;
    }

    /** Returns the architecture on which the library is running. */
    public static Architecture getArchitecture() {
        return Architecture.current;
    }

    public static String mapLibraryNameBundled(String name) {
        return bundledLibraryNameMapper.apply(name);
    }

    static String mapLibraryPathBundled(String name) {
        return bundledLibraryPathMapper.apply(name);
    }

    @SuppressWarnings("unchecked")
    private static Function<String, String> getMapper(
        @Nullable Object mapper,
        Function<String, String> defaultMapper,
        Function<String, String> legacyMapper
    ) {
        if (mapper == null || "default".equals(mapper)) return defaultMapper;
        if ("legacy".equals(mapper)) return legacyMapper;
        if (mapper instanceof Function) return (Function<String, String>)mapper;

        String className = mapper.toString();

        try {
            return (Function<String, String>) Class.forName(className).getConstructor().newInstance();
        } catch (Throwable t) {
            if (DEBUG) t.printStackTrace(DEBUG_STREAM);
            JNILibraryLoader.log(String.format("Warning: Failed to instantiate bundled library mapper: %s. Using the default.", className));

            return defaultMapper;
        }
    }

}
