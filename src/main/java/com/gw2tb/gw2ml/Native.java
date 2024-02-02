package com.gw2tb.gw2ml;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

import static java.lang.foreign.ValueLayout.*;

final class Native {

    public static final MemorySegment INVALID_HANDLE_VALUE = MemorySegment.ofAddress(-1L);

    public static final int FILE_MAP_READ   = 0x0002,
                            FILE_MAP_WRITE  = 0x0004;

    public static final int PAGE_EXECUTE_READWRITE = 0x40;

    private static final Linker linker = Linker.nativeLinker();
    private static final SymbolLookup symbolLookup = SymbolLookup.libraryLookup("Kernel32", Arena.ofAuto());

    // https://learn.microsoft.com/en-us/windows/win32/api/handleapi/nf-handleapi-closehandle
    private static final FunctionDescriptor DESC_CloseHandle = FunctionDescriptor.of(
        JAVA_BOOLEAN, ADDRESS
    );
    private static final MethodHandle MH_CloseHandle = symbolLookup.find("CloseHandle").map(addr -> linker.downcallHandle(addr, DESC_CloseHandle)).orElseThrow();

    public static boolean CloseHandle(MemorySegment handle) {
        try {
            return (boolean) MH_CloseHandle.invokeExact(handle);
        } catch (Throwable t) {
            throw new AssertionError("Should never be reached", t);
        }
    }

    // https://learn.microsoft.com/en-us/windows/win32/api/memoryapi/nf-memoryapi-createfilemappingw
    private static final FunctionDescriptor DESC_CreateFileMapping = FunctionDescriptor.of(
        ADDRESS, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS
    );
    private static final MethodHandle MH_CreateFileMapping = symbolLookup.find("CreateFileMappingW").map(addr -> linker.downcallHandle(addr, DESC_CreateFileMapping)).orElseThrow();

    public static MemorySegment CreateFileMapping(MemorySegment hFile, MemorySegment lpFileMappingAttributes, int flProtect, int dwMaximumSizeHigh, int dwMaximumSizeLow, String lpName) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment _lpName = arena.allocateFrom(lpName, StandardCharsets.UTF_16LE);
            return (MemorySegment) MH_CreateFileMapping.invokeExact(hFile, lpFileMappingAttributes, flProtect, dwMaximumSizeHigh, dwMaximumSizeLow, _lpName);
        } catch (Throwable t) {
            throw new AssertionError("Should never be reached", t);
        }
    }

    // https://learn.microsoft.com/en-us/windows/win32/api/memoryapi/nf-memoryapi-mapviewoffile
    private static final FunctionDescriptor DESC_MapViewOfFile = FunctionDescriptor.of(
        ADDRESS, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS
    );
    private static final MethodHandle MH_MapViewOfFile = symbolLookup.find("MapViewOfFile").map(addr -> linker.downcallHandle(addr, DESC_MapViewOfFile)).orElseThrow();

    public static MemorySegment MapViewOfFile(MemorySegment hFileMappingObject, int dwDesiredAccess, int dwFileOffsetHigh, int dwFileOffsetLow, long dwNumberOfBytesToMap) {
        try {
            return (MemorySegment) MH_MapViewOfFile.invokeExact(hFileMappingObject, dwDesiredAccess, dwFileOffsetHigh, dwFileOffsetLow, MemorySegment.ofAddress(dwNumberOfBytesToMap));
        } catch (Throwable t) {
            throw new AssertionError("Should never be reached", t);
        }
    }

    // https://learn.microsoft.com/en-us/windows/win32/api/memoryapi/nf-memoryapi-openfilemappingw
    private static final FunctionDescriptor DESC_OpenFileMapping = FunctionDescriptor.of(
        ADDRESS, JAVA_INT, JAVA_BOOLEAN, ADDRESS
    );
    private static final MethodHandle MH_OpenFileMapping = symbolLookup.find("OpenFileMappingW").map(addr -> linker.downcallHandle(addr, DESC_OpenFileMapping)).orElseThrow();

    public static MemorySegment OpenFileMapping(int dwDesiredAccess, boolean bInheritHandle, String lpName) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment _lpName = arena.allocateFrom(lpName, StandardCharsets.UTF_16LE);
            return (MemorySegment) MH_OpenFileMapping.invokeExact(dwDesiredAccess, bInheritHandle, _lpName);
        } catch (Throwable t) {
            throw new AssertionError("Should never be reached", t);
        }
    }

    // https://learn.microsoft.com/en-us/windows/win32/api/memoryapi/nf-memoryapi-unmapviewoffile
    private static final FunctionDescriptor DESC_UnmapViewOfFile = FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS);
    private static final MethodHandle MH_UnmapViewOfFile = symbolLookup.find("UnmapViewOfFile").map(addr -> linker.downcallHandle(addr, DESC_UnmapViewOfFile)).orElseThrow();

    public static boolean UnmapViewOfFile(MemorySegment lpBaseAddress) {
        try {
            return (boolean) MH_UnmapViewOfFile.invokeExact(lpBaseAddress);
        } catch (Throwable t) {
            throw new AssertionError("Should never be reached", t);
        }
    }

}