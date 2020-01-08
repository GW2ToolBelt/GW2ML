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

import java.io.PrintStream;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * This class can be used to programmatically configure GW2ML at runtime.
 *
 * <p>Care must be taken when setting <em>static</em> options. Such options are only read once or cached in
 * {@code static final} fields. They must be configured through this class before touching any other GW2ML class.</p>
 *
 * @since   0.1.0
 *
 * @author  Leon Linhart
 */
public final class Configuration<T> {

    /**
     * Takes priority over {@code java.library.path}. It may contain one or more directory paths, separated by
     * {@link java.io.File#pathSeparator}.
     *
     * <p style="font-family: monospace">
     * Property: <b>com.github.gw2toolbelt.gw2ml.librarypath</b><br>
     * &nbsp; &nbsp;Usage: Dynamic<br>
     *
     * @since   0.1.0
     */
    public static final Configuration<String> LIBRARY_PATH = new Configuration<>(
        "com.github.gw2toolbelt.gw2ml.librarypath",
        StateInit.STRING,
        Usage.DYNAMIC
    );

    /**
     * Sets the mapping algorithm used to resolve the <b>name</b> of bundled shared libraries. Supported values:
     *
     * <ul>
     * <li><em>default</em> - Maps {@code <libname>} to {@code <libname>}.</li>
     * <li><em>legacy</em> - Maps {@code <libname>} to {@code is64bit(arch) ? <libname> : <libname>32}.</li>
     * <li><em>&lt;classpath&gt;</em> - A class that implements the {@link Function Function&lt;String, String&gt;} interface. It will be instantiated using reflection.</li>
     * </ul>
     *
     * <p>When set programmatically, it can also be a {@link Function Function&lt;String, String&gt;} instance.</p>
     *
     * <p style="font-family: monospace">
     * Property: <b>com.github.gw2toolbelt.gw2ml.bundledLibrary.nameMapper</b><br>
     * &nbsp; &nbsp; Type: String or a {@link Function Function&lt;String, String&gt;} instance<br>
     * &nbsp; &nbsp;Usage: Static</p>
     *
     * @since   0.1.0
     */
    public static final Configuration<Object> BUNDLED_LIBRARY_NAME_MAPPER = new Configuration<>(
        "com.github.gw2toolbelt.gw2ml.bundledLibrary.nameMapper",
        StateInit.STRING,
        Usage.STATIC
    );

    /**
     * Sets the mapping algorithm used to resolve bundled shared libraries in the <b>classpath/modulepath</b>. Supported values:
     *
     * <ul>
     * <li><em>default</em> - Maps {@code <libpath>} to {@code <arch>/<libpath>}.</li>
     * <li><em>legacy</em> - Maps {@code <libpath>} to {@code <libpath>}.</li>
     * <li><em>&lt;classpath&gt;</em> - A class that implements the {@link Function Function&lt;String, String&gt;} interface. It will be instantiated using reflection.</li>
     * </ul>
     *
     * <p>When set programmatically, it can also be a {@link Function Function&lt;String, String&gt;} instance.</p>
     *
     * <p style="font-family: monospace">
     * Property: <b>com.github.gw2toolbelt.gw2ml.bundledLibrary.nameMapper</b><br>
     * &nbsp; &nbsp; Type: String or a {@link Function Function&lt;String, String&gt;} instance<br>
     * &nbsp; &nbsp;Usage: Static</p>
     *
     * @since   0.1.0
     */
    public static final Configuration<Object> BUNDLED_LIBRARY_PATH_MAPPER = new Configuration<>(
        "com.github.gw2toolbelt.gw2ml.bundledLibrary.pathMapper",
        StateInit.STRING,
        Usage.STATIC
    );

    /**
     * Changes the temporary directory name created by GW2ML when extracting shared libraries from JAR files. If this
     * option is not set, it defaults to <code>gw2ml&lt;user name&gt;</code>.
     *
     * <p style="font-family: monospace">
     * Property: <b>com.github.gw2toolbelt.gw2ml.SharedLibraryExtractDirectory</b><br>
     * &nbsp; &nbsp;Usage: Dynamic<br>
     *
     * @since   0.1.0
     */
    public static final Configuration<String> SHARED_LIBRARY_EXTRACT_DIRECTORY = new Configuration<>(
        "com.github.gw2toolbelt.gw2ml.SharedLibraryExtractDirectory",
        StateInit.STRING,
        Usage.DYNAMIC
    );

    /**
     * Changes the path where GW2ML extracts shared libraries from JAR files. If this option is not set, GW2ML will try
     * the following paths and the first successful will be used:
     *
     * <ul>
     * <li>{@code System.getProperty("java.io.tmpdir")}/extractDir/version/</li>
     * <li>{@code System.getProperty("user.home")}/.extractDir/version/</li>
     * <li>.extractDir/version/</li>
     * <li>{@code Files.createTempFile("gw2ml", "")}</li>
     * </ul>
     *
     * where:
     *
     * <pre><code>
     * extractDir = Configuration.SHARED_LIBRARY_EXTRACT_DIRECTORY
     * version = apiGetManifestValue("Implementation-Version").orElse("dev")
     * </code></pre>
     *
     * <p style="font-family: monospace">
     * Property: <b>com.github.gw2toolbelt.gw2ml.SharedLibraryExtractPath</b><br>
     * &nbsp; &nbsp;Usage: Dynamic</p>
     *
     * @since   0.1.0
     */
    public static final Configuration<String> SHARED_LIBRARY_EXTRACT_PATH = new Configuration<>(
        "com.github.gw2toolbelt.gw2ml.SharedLibraryExtractPath",
        StateInit.STRING,
        Usage.DYNAMIC
    );

    /**
     * Can be used to override the GW2ML library name. It can be an absolute path.
     *
     * <p style="font-family: monospace">
     * Property: <b>com.github.gw2toolbelt.gw2ml.libname</b><br>
     * &nbsp; &nbsp;Usage: Dynamic</p>
     *
     * @since   0.1.0
     */
    public static final Configuration<String> LIBRARY_NAME = new Configuration<>(
        "com.github.gw2toolbelt.gw2ml.libname",
        StateInit.STRING,
        Usage.DYNAMIC
    );

    /**
     * Set to true to enable GW2ML's debug mode. Information messages will be printed to the {@link #DEBUG_STREAM} and
     * extra runtime checks will be performed (some potentially expensive, performance-wise).
     *
     * <p style="font-family: monospace">
     * Property: <b>com.github.gw2toolbelt.gw2ml.util.Debug</b><br>
     * &nbsp; &nbsp;Usage: Static</p>
     *
     * @since   0.1.0
     */
    public static final Configuration<Boolean> DEBUG = new Configuration<>(
        "com.github.gw2toolbelt.gw2ml.util.Debug",
        StateInit.BOOLEAN,
        Usage.STATIC
    );

    /**
     * When enabled, ShaderLibraryLoader exceptions will be printed to the {@link #DEBUG_STREAM}.
     *
     * <p>This option requires {@link #DEBUG} to be enabled.</p>
     *
     * <p style="font-family: monospace">
     * Property: <b>com.github.gw2toolbelt.gw2ml.util.DebugLoader</b><br>
     * &nbsp; &nbsp;Usage: Static</p>
     *
     * @since   0.1.0
     */
    public static final Configuration<Boolean> DEBUG_LOADER = new Configuration<>(
        "com.github.gw2toolbelt.gw2ml.util.DebugLoader",
        StateInit.BOOLEAN,
        Usage.STATIC
    );

    /**
     * Can be set to override the default debug stream. It must be the name of a class that implements the
     * {@link Supplier Supplier&lt;PrintStream&gt;} interface. The class will be instantiated using reflection and the
     * result of {@link Supplier#get get} will become the {@code #DEBUG_STREAM} used by GW2ML.
     *
     * <p>When set programmatically, it can also be a {@link PrintStream} instance.</p>
     *
     * <p style="font-family: monospace">
     * Property: <b>com.github.gw2toolbelt.gw2ml.util.DebugStream</b><br>
     * &nbsp; &nbsp; Type: String or a {@link PrintStream} instance<br>
     * &nbsp; &nbsp;Usage: Static</p>
     *
     * @since   0.1.0
     */
    public static final Configuration<Object> DEBUG_STREAM = new Configuration<>(
        "com.github.gw2toolbelt.gw2ml.util.DebugStream",
        StateInit.STRING,
        Usage.STATIC
    );

    /**
     * Set to true to disable GW2ML's basic checks. These are trivial checks that GW2ML performs, very useful during
     * development. Their performance impact is usually minimal, but they may be disabled for release builds.
     *
     * <p style="font-family: monospace">
     * Property: <b>com.github.gw2toolbelt.gw2ml.util.NoChecks</b><br>
     * &nbsp; &nbsp;Usage: Static</p>
     *
     * @since   0.1.0
     */
    public static final Configuration<Boolean> DISABLE_CHECKS = new Configuration<>(
        "com.github.gw2toolbelt.gw2ml.util.NoChecks",
        StateInit.BOOLEAN,
        Usage.STATIC
    );

    private interface StateInit<T> extends Function<String, T> {

        StateInit<Boolean> BOOLEAN = property -> {
            String value = System.getProperty(property);
            return value == null ? null : Boolean.parseBoolean(value);
        };

        StateInit<String> STRING = System::getProperty;

    }

    private enum Usage {
        DYNAMIC,
        STATIC
    }

    private final String property;
    private final Usage usage;

    @Nullable
    private T state;
    private boolean isFrozen;

    private Configuration(String property, StateInit<? extends T> init, Usage usage) {
        this.property = property;
        this.state = init.apply(property);
        this.usage = usage;
    }

    /**
     * Returns the property key.
     *
     * @return  the property key
     *
     * @since   0.1.0
     */
    String getProperty() {
        return property;
    }

    /**
     * Sets the option value.
     *
     * @param value the value to set
     *
     * @since   0.1.0
     */
    public void set(@Nullable T value) {
        if (this.isFrozen) throw new IllegalStateException("Property is statically used.");
        this.state = value;
    }

    /**
     * Returns the value of this option, or {@code null} if no value has been set.
     *
     * @return  the value of this option, or {@code null} if no value has been set
     *
     * @since   0.1.0
     */
    @Nullable
    public T get() {
        if (this.usage == Usage.STATIC) this.isFrozen = true;
        return state;
    }

    /**
     * Returns the value of this option, or {@code defaultValue} if no value has been set.
     *
     * @param defaultValue  the default value
     *
     * @return  the value of this option, or {@code defaultValue} if no value has been set
     *
     * @since   0.1.0
     */
    public T get(T defaultValue) {
        if (this.usage == Usage.STATIC) this.isFrozen = true;

        T state = this.state;
        if (state == null) state = defaultValue;

        return state;
    }

}