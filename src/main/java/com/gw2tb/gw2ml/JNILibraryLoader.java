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
package com.gw2tb.gw2ml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * JNI native library loading tools.
 *
 * @author  LWJGL
 */
final class JNILibraryLoader {

    /** Whether or not debug output is enabled. */
    static final boolean DEBUG = Configuration.DEBUG.get(false);

    /** Whether or not additional checks are enabled. */
    static final boolean CHECKS = !Configuration.DISABLE_CHECKS.get(false);

    static {
        if (DEBUG) {
            log("\t OS: " + System.getProperty("os.name") + " v" + System.getProperty("os.version"));
            log("\tJRE: " + System.getProperty("java.version") + " " + System.getProperty("os.arch"));
            log("\tJVM: " + System.getProperty("java.vm.name") + " v" + System.getProperty("java.vm.version") + " by " + System.getProperty("java.vm.vendor"));
        }
    }

    /**
     * The {@link PrintStream} used by GW2ML to print debug information and non-fatal errors.
     *
     * <p>Defaults to {@link System#err} which can be changed with {@link Configuration#DEBUG_STREAM}.</p>
     */
    static final PrintStream DEBUG_STREAM = getDebugStream();

    @SuppressWarnings("unchecked")
    private static PrintStream getDebugStream() {
        PrintStream debugStream = System.err;

        Object state = Configuration.DEBUG_STREAM.get();
        if (state instanceof String) {
            try {
                Supplier<PrintStream> factory = (Supplier<PrintStream>) Class.forName((String) state).getConstructor().newInstance();
                debugStream = factory.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (state instanceof Supplier<?>) {
            debugStream = ((Supplier<PrintStream>) state).get();
        } else if (state instanceof PrintStream) {
            debugStream = (PrintStream) state;
        }

        return debugStream;
    }

    /**
     * Prints the specified message to the {@link #DEBUG_STREAM} if {@link #DEBUG} is {@code true}.
     *
     * @param msg the message to print
     */
    static void log(String msg) {
        if (DEBUG) {
            DEBUG_STREAM.print("[GW2ML] ");
            DEBUG_STREAM.println(msg);
        }
    }

    private static final String JAVA_LIBRARY_PATH = "java.library.path";
    private static final Pattern PATH_SEPARATOR = Pattern.compile(File.pathSeparator);
    private static final Pattern NATIVES_JAR = Pattern.compile("/[\\w-]+?-natives-\\w+.jar!/");

    /** Calls {@link #loadSystem(Consumer, Consumer, Class, String, String)} using {@code JNILibraryLoader.class} as the context parameter. */
    public static void loadSystem(String module, String name) throws UnsatisfiedLinkError {
        loadSystem(System::load, System::loadLibrary, JNILibraryLoader.class, module, name);
    }

    /**
     * Loads a JNI shared library.
     *
     * @param load        should be the {@code System::load} expression. This ensures that {@code System.load} has the same caller as this method.
     * @param loadLibrary should be the {@code System::loadLibrary} expression. This ensures that {@code System.loadLibrary} has the same caller as this
     *                    method.
     * @param context     the class to use to discover the shared library in the classpath
     * @param module      the module to which the shared library belongs
     * @param name        the library name. If not an absolute path, it must be the plain library name, without an OS specific prefix or file extension (e.g.
     *                    GL, not libGL.so)
     *
     * @throws UnsatisfiedLinkError if the library could not be loaded
     */
    public static void loadSystem(
        Consumer<String> load,
        Consumer<String> loadLibrary,
        Class<?> context,
        String module,
        String name
    ) throws UnsatisfiedLinkError {
        log("Loading JNI library: " + name);
        log("\tModule: " + module);

        // METHOD 1: absolute path
        if (Paths.get(name).isAbsolute()) {
            load.accept(name);
            log("\tSuccess");
            return;
        }

        String libName = Platform.get().mapLibraryName(name);

        // METHOD 2: com.gw2tb.gw2ml.librarypath
        URL libURL = findResource(context, module, libName);
        if (libURL == null) {
            if (loadSystemFromLibraryPath(load, context, module, libName)) {
                return;
            }
        } else {
            // Always use the SLL if the library is found in the classpath,
            // so that newer versions can be detected.
            boolean debugLoader = Configuration.DEBUG_LOADER.get(false);

            try {
                String regular = getRegularFilePath(libURL);
                if (regular != null) {
                    load.accept(regular);
                    log("\tLoaded from classpath: " + regular);
                    return;
                }

                if (debugLoader) log("\tUsing SharedLibraryLoader...");

                // Extract from classpath and try com.gw2tb.gw2ml.librarypath
                try (FileChannel ignored = SharedLibraryLoader.load(name, libName, libURL)) {
                    if (loadSystemFromLibraryPath(load, context, module, libName)) {
                        return;
                    }
                }
            } catch (Exception e) {
                if (debugLoader) e.printStackTrace(DEBUG_STREAM);
            }
        }

        String javaLibraryPath = System.getProperty(JAVA_LIBRARY_PATH);

        // METHOD 3: java.library.path (bundled only)
        if (javaLibraryPath != null) {
            if (loadSystem(load, context, module, getBundledPath(module, libName), false, JAVA_LIBRARY_PATH, javaLibraryPath)) {
                return;
            }
        }

        // METHOD 4: System.loadLibrary
        try {
            loadLibrary.accept(name);

            // Success, but java.library.path might be empty, or not include the library.
            // In that case, ClassLoader::findLibrary was used to return the library path (e.g. OSGi does this with native libraries in bundles).
            Path libFile = javaLibraryPath == null ? null : findFile(javaLibraryPath, module, libName, true);
            if (libFile != null) {
                log(String.format("\tLoaded from %s: %s", JAVA_LIBRARY_PATH, libFile));
                checkHash(context, libFile);
            } else {
                log("\tLoaded from a ClassLoader provided path.");
            }
            return;
        } catch (Throwable t) {
            log(String.format("\t%s not found in %s", libName, JAVA_LIBRARY_PATH));
        }

        printError();
        throw new UnsatisfiedLinkError("Failed to locate library: " + libName);
    }

    private static boolean loadSystemFromLibraryPath(Consumer<String> load, Class<?> context, String module, String libName) {
        String paths = Configuration.LIBRARY_PATH.get();
        return paths != null && loadSystem(load, context, module, libName, true, Configuration.LIBRARY_PATH.getProperty(), paths);
    }

    private static boolean loadSystem(Consumer<String> load, Class<?> context, String module, String libName, boolean bundledWithGW2ML, String property, String paths) {
        Path libFile = findFile(paths, module, libName, bundledWithGW2ML);
        if (libFile == null) {
            log(String.format("\t%s not found in %s=%s", libName, property, paths));
            return false;
        }

        load.accept(libFile.toAbsolutePath().toString());
        log(String.format("\tLoaded from %s: %s", property, libFile));
        checkHash(context, libFile);
        return true;
    }

    private static String getBundledPath(String module, String resource) {
        return Platform.mapLibraryPathBundled(module.replace('.', '/') + "/" + resource);
    }

    @Nullable
    private static URL findResource(Class<?> context, String module, String resource) {
        URL url = null;

        String bundledResource = getBundledPath(module, resource);

        if (!bundledResource.equals(resource)) {
            url = context.getClassLoader().getResource(bundledResource);
        }

        return url == null ? context.getClassLoader().getResource(resource) : url;
    }

    @Nullable
    private static String getRegularFilePath(URL url) {
        if (url.getProtocol().equals("file")) {
            try {
                Path path = Paths.get(url.toURI());
                if (path.isAbsolute() && Files.isReadable(path)) {
                    return path.toString();
                }
            } catch (URISyntaxException ignored) {}
        }

        return null;
    }

    @Nullable
    private static Path findFile(String path, String module, String file, boolean bundledWithGW2ML) {
        if (bundledWithGW2ML) {
            String bundledFile = getBundledPath(module, file);

            if (!bundledFile.equals(file)) {
                Path p = findFile(path, bundledFile);
                if (p != null) return p;
            }
        }

        return findFile(path, file);
    }

    @Nullable
    private static Path findFile(String path, String file) {
        for (String directory : PATH_SEPARATOR.split(path)) {
            Path p = Paths.get(directory, file);
            if (Files.isReadable(p)) return p;
        }

        return null;
    }

    private static void printError() {
        printError(
            "[GW2ML] Failed to load a library. Possible solutions:\n" +
                "\ta) Add the directory that contains the shared library to -Djava.library.path or -Dcom.gw2tb.gw2ml.librarypath.\n" +
                "\tb) Add the JAR that contains the shared library to the classpath."
        );
    }

    private static void printError(String message) {
        DEBUG_STREAM.println(message);

        if (!DEBUG) {
            DEBUG_STREAM.println("[GW2ML] Enable debug mode with -Dcom.gw2tb.gw2ml.util.Debug=true for better diagnostics.");

            if (!Configuration.DEBUG_LOADER.get(false))
                DEBUG_STREAM.println("[GW2ML] Enable the SharedLibraryLoader debug mode with -Dcom.gw2tb.gw2ml.util.DebugLoader=true for better diagnostics.");
        }
    }

    /**
     * Compares the shared library hash stored in the classpath, with the hash of the actual library loaded at runtime.
     *
     * <p>This check prints a simple warning when there's a hash mismatch, to help diagnose installation/classpath issues. It is not a security feature.</p>
     *
     * @param context the class to use to discover the shared library hash in the classpath
     * @param libFile the library file loaded
     */
    private static void checkHash(Class<?> context, Path libFile) {
        if (!CHECKS) return;

        try {
            URL classesURL = null;
            URL nativesURL = null;

            Enumeration<URL> resources = context.getClassLoader().getResources(libFile.getFileName() + ".sha1");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if (NATIVES_JAR.matcher(url.toExternalForm()).find()) {
                    nativesURL = url;
                } else {
                    classesURL = url;
                }
            }
            if (classesURL == null) {
                return;
            }

            byte[] expected = getSHA1(classesURL);
            byte[] actual = DEBUG || nativesURL == null
                ? getSHA1(libFile)
                : getSHA1(nativesURL);

            if (!Arrays.equals(expected, actual)) {
                DEBUG_STREAM.println(
                    "[GW2ML] [ERROR] Incompatible Java and native library versions detected.\n" +
                        "Possible reasons:\n" +
                        "\ta) -Djava.library.path is set to a folder containing shared libraries of an older GW2ML version.\n" +
                        "\tb) The classpath contains jar files of an older GW2ML version.\n" +
                        "Possible solutions:\n" +
                        "\ta) Make sure to not set -Djava.library.path (it is not needed for developing with GW2ML) or make\n" +
                        "\t   sure the folder it points to contains the shared libraries of the correct GW2ML version.\n" +
                        "\tb) Check the classpath and make sure to only have jar files of the same GW2ML version in it.");
            }
        } catch (Throwable t) {
            if (DEBUG) {
                log("Failed to verify native library.");
                t.printStackTrace();
            }
        }
    }

    private static byte[] getSHA1(URL hashURL) throws IOException {
        byte[] hash = new byte[20];

        try (InputStream sha1 = hashURL.openStream()) {
            for (int i = 0; i < 20; i++) {
                hash[i] = (byte) ((Character.digit(sha1.read(), 16) << 4) | Character.digit(sha1.read(), 16));
            }
        }

        return hash;
    }

    private static byte[] getSHA1(Path libFile) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        try (InputStream input = Files.newInputStream(libFile)) {
            byte[] buffer = new byte[8 * 1024];
            for (int n; (n = input.read(buffer)) != -1; ) digest.update(buffer, 0, n);
        }

        return digest.digest();
    }

}