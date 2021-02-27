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
 * Copyright (c) 2019-2021 Leon Linhart
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
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * Loads shared libraries and native resources from the classpath.
 *
 * <p>The libraries may be packed in JAR files, in which case they will be extracted to a temporary directory and that
 * directory will be prepended to {@link Configuration#LIBRARY_PATH}.</p>
 *
 * @see Configuration#SHARED_LIBRARY_EXTRACT_DIRECTORY
 * @see Configuration#SHARED_LIBRARY_EXTRACT_PATH
 *
 *
 * @author Mario Zechner (https://github.com/badlogic)
 * @author Nathan Sweet (https://github.com/NathanSweet)
 */
final class SharedLibraryLoader {

    private static final Lock EXTRACT_PATH_LOCK = new ReentrantLock();

    @GuardedBy("EXTRACT_PATH_LOCK")
    @Nullable
    private static Path extractPath;

    /**
     * Extracts the specified shared library or native resource from the classpath to a temporary directory.
     *
     * @param name     the resource name
     * @param filename the resource filename
     * @param resource the classpath {@link URL} were the resource can be found
     *
     * @return a {@link FileChannel} that has locked the resource file
     */
    static FileChannel load(String name, String filename, URL resource) {
        try {
            Path extractedFile;

            EXTRACT_PATH_LOCK.lock();

            try {
                if (extractPath != null) {
                    // Reuse the GW2ML shared library location
                    extractedFile = extractPath.resolve(filename);
                } else {
                    extractedFile = getExtractPath(filename, resource);
                    // Do not store unless the test for JDK-8195129 has passed
                    if (Platform.get() != Platform.WINDOWS || workaroundJDK8195129(extractedFile)) {
                        initExtractPath(extractPath = extractedFile.getParent());
                    }
                }
            } finally {
                EXTRACT_PATH_LOCK.unlock();
            }

            return extract(extractedFile, resource);
        } catch (Exception e) {
            throw new RuntimeException("\tFailed to extract " + name + " library", e);
        }
    }

    private static void initExtractPath(Path extractPath) {
        String newLibPath = extractPath.toAbsolutePath().toString();

        // Prepend the path in which the libraries were extracted to com.gw2tb.gw2ml.librarypath
        String libPath = Configuration.LIBRARY_PATH.get();
        if (libPath != null && !libPath.isEmpty()) newLibPath += File.pathSeparator + libPath;

        System.setProperty(Configuration.LIBRARY_PATH.getProperty(), newLibPath);
        Configuration.LIBRARY_PATH.set(newLibPath);
    }

    /**
     * Returns a path to a file that can be written. Tries multiple locations and verifies writing succeeds.
     *
     * @param filename the resource filename
     *
     * @return the extracted library
     */
    private static Path getExtractPath(String filename, URL resource) {
        String override = Configuration.SHARED_LIBRARY_EXTRACT_PATH.get();
        if (override != null) return Paths.get(override, filename);

        String version = MumbleLink.GW2ML_VERSION;

        Path tempDirectory;
        Path root, file;

        // Temp directory with username in path
        tempDirectory = Paths.get(Configuration.SHARED_LIBRARY_EXTRACT_DIRECTORY.get("gw2ml" + System.getProperty("user.name")), version, filename);
        file = (root = Paths.get(System.getProperty("java.io.tmpdir"))).resolve(tempDirectory);
        if (canWrite(root, file, resource)) return file;

        // User home
        tempDirectory = Paths.get(Configuration.SHARED_LIBRARY_EXTRACT_DIRECTORY.get("gw2ml"), version, filename);
        file = (root = Paths.get(System.getProperty("user.home"))).resolve(tempDirectory);
        if (canWrite(root, file, resource)) return file;

        // Working directory
        file = (root = Paths.get("").toAbsolutePath()).resolve(tempDirectory);
        if (canWrite(root, file, resource)) return file;

        if (Platform.get() == Platform.WINDOWS) {
            // C:\Windows\Temp
            String env = System.getenv("SystemRoot");
            if (env != null) {
                file = (root = Paths.get(env, "Temp")).resolve(tempDirectory);
                if (canWrite(root, file, resource)) return file;
            }

            // C:\Temp
            env = System.getenv("SystemDrive");
            if (env != null) {
                file = (root = Paths.get(env + "/")).resolve(Paths.get("Temp").resolve(tempDirectory));
                if (canWrite(root, file, resource)) return file;
            }
        }

        // System provided temp directory (in java.io.tmpdir)
        try {
            file = Files.createTempDirectory("gw2ml");
            root = file.getParent();
            file = file.resolve(filename);

            if (canWrite(root, file, resource)) return file;
        } catch (IOException ignored) {}

        throw new RuntimeException("Failed to find an appropriate directory to extract the native library");
    }

    /**
     * Extracts a native library resource if it does not already exist or the CRC does not match.
     *
     * @param resource the resource to extract
     * @param file     the extracted file
     *
     * @return a {@link FileChannel} that has locked the resource
     *
     * @throws IOException if an IO error occurs
     */
    private static FileChannel extract(Path file, URL resource) throws IOException {
        if (Files.exists(file)) {
            try (
                InputStream source = resource.openStream();
                InputStream target = Files.newInputStream(file)
            ) {
                if (crc(source) == crc(target)) {
                    if (Configuration.DEBUG_LOADER.get(false)) JNILibraryLoader.log(String.format("\tFound at: %s", file));
                    return lock(file);
                }
            }
        }

        // If file doesn't exist or the CRC doesn't match, extract it to the temp dir.
        JNILibraryLoader.log(String.format("    Extracting: %s", resource.getPath()));
        if (extractPath == null) JNILibraryLoader.log(String.format("            to: %s", file));

        Files.createDirectories(file.getParent());
        try (InputStream source = resource.openStream()) {
            Files.copy(source, file, StandardCopyOption.REPLACE_EXISTING);
        }

        return lock(file);
    }

    /**
     * Locks a file.
     *
     * @param file the file to lock
     */
    private static FileChannel lock(Path file) {
        /*
         * Wait for other processes (usually antivirus software) to unlock the extracted file before attempting to
         * load it.
         */
        try {
            FileChannel fc = FileChannel.open(file);

            if (fc.tryLock(0L, Long.MAX_VALUE, true) == null) {
                if (Configuration.DEBUG_LOADER.get(false)) {
                    JNILibraryLoader.log("\tFile is locked by another process, waiting...");
                }

                fc.lock(0L, Long.MAX_VALUE, true); // This will block until the file is locked.
            }

            return fc; // The lock will be released when the channel is closed.
        } catch (Exception e) {
            throw new RuntimeException("Failed to lock file.", e);
        }
    }

    /**
     * Returns a CRC of the remaining bytes in a stream.
     *
     * @param input the stream
     *
     * @return the CRC
     */
    private static long crc(InputStream input) throws IOException {
        CRC32 crc = new CRC32();

        byte[] buffer = new byte[8 * 1024];
        for (int n; (n = input.read(buffer)) != -1; ) crc.update(buffer, 0, n);

        return crc.getValue();
    }

    /**
     * Returns true if the parent directories of the file can be created and the file can be written.
     *
     * @param file the file to test
     *
     * @return true if the file is writable
     */
    private static boolean canWrite(Path root, Path file, URL resource) {
        Path testFile;

        if (Files.exists(file)) {
            if (!Files.isWritable(file)) return false;

            // Don't overwrite existing file just to check if we can write to directory.
            testFile = file.getParent().resolve(".gw2ml.test");
        } else {
            try {
                Files.createDirectories(file.getParent());
            } catch (IOException ignored) {
                return false;
            }

            testFile = file;
        }

        try {
            Files.write(testFile, new byte[0]);
            Files.delete(testFile);

            if (workaroundJDK8195129(file)) {
                /*
                 * We have full access, the JVM has locked the file, but System.load can still fail if the path contains
                 * unicode characters, due to JDK-8195129. Test for this here and return false if it fails to try other
                 * paths.
                 */
                try (FileChannel ignored = extract(file, resource)) {
                    System.load(file.toAbsolutePath().toString());
                }
            }

            return true;
        } catch (Throwable ignored) {
            if (file == testFile) canWriteCleanup(root, file);
            return false;
        }
    }

    private static void canWriteCleanup(Path root, Path file) {
        try {
            // Remove any files or directories created by canWrite.
            Files.deleteIfExists(file);

            // Delete empty directories from parent down to root (exclusive).
            Path parent = file.getParent();
            while (!Files.isSameFile(parent, root)) {
                try (Stream<Path> dir = Files.list(parent)) {
                    if (dir.findAny().isPresent()) break;
                }

                Files.delete(parent);
                parent = parent.getParent();
            }
        } catch (IOException ignored) {}
    }

    private static boolean workaroundJDK8195129(Path file) {
        return Platform.get() == Platform.WINDOWS && file.toString().endsWith(".dll");
    }

    // This utility class only provides static functionality and is not meant to be initialized.
    private SharedLibraryLoader() {}

}