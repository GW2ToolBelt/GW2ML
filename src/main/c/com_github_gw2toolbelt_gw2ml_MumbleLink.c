/*
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
#pragma warning(push, 0)
#include <windows.h>
#include <jni.h>
#pragma warning(pop)

#include <stdint.h>

#define MUMBLE_LINK_BYTES 5460

#define UNUSED_PARAM(param) \
    (void)(param);

typedef struct GW2MLinstance {
    HANDLE  hFileMapping;
    void*   linkedMem;
} GW2MLinstance;

inline jint throwIllegalStateException(JNIEnv *env, char *message) {
    jclass clazz = (*env)->FindClass(env, "java/lang/NoClassDefFoundError");
    return (*env)->ThrowNew(env, clazz, message);
}

JNIEXPORT jobject JNICALL Java_com_github_gw2toolbelt_gw2ml_MumbleLink_nOpen(JNIEnv* env, jclass clazz, jstring handle) {
    UNUSED_PARAM(clazz);

    const char* handleName = (*env)->GetStringUTFChars(env, handle, NULL);

    HANDLE hFileMapping = OpenFileMappingA(FILE_MAP_READ, FALSE, handleName);
    if (hFileMapping == NULL) {
        hFileMapping = CreateFileMappingA(INVALID_HANDLE_VALUE, NULL, PAGE_READWRITE, 0, MUMBLE_LINK_BYTES, handleName);
        (*env)->ReleaseStringUTFChars(env, handle, handleName);

        if (hFileMapping == NULL) throwIllegalStateException(env, "Failed to create FileMapping.");
    } else {
        (*env)->ReleaseStringUTFChars(env, handle, handleName);
    }

    void* linkedMem = (void*) MapViewOfFile(hFileMapping, FILE_MAP_READ, 0, 0, MUMBLE_LINK_BYTES);
    if (linkedMem == NULL) {
        CloseHandle(hFileMapping);
        hFileMapping = NULL;
        return NULL;
    }

    GW2MLinstance* instance = malloc(sizeof(GW2MLinstance));
    instance->hFileMapping = hFileMapping;
    instance->linkedMem = linkedMem;

    jobject buffer = (*env)->NewDirectByteBuffer(env, linkedMem, MUMBLE_LINK_BYTES);
    if (buffer == NULL) {
        UnmapViewOfFile(linkedMem);
        CloseHandle(hFileMapping);
        if (hFileMapping == NULL) throwIllegalStateException(env, "Failed to create new direct ByteBuffer.");
    }

    jclass cls = (*env)->FindClass(env, "com/github/gw2toolbelt/gw2ml/MumbleLink");
    if (!cls) {
        UnmapViewOfFile(linkedMem);
        CloseHandle(hFileMapping);
        if (hFileMapping == NULL) throwIllegalStateException(env, "Failed to find MumbleLink class.");
    }

    jmethodID cid = (*env)->GetMethodID(env, cls, "<init>", "(JLjava/nio/ByteBuffer;Ljava/lang/String;)V");
    if (!cid) {
        UnmapViewOfFile(linkedMem);
        CloseHandle(hFileMapping);
        if (hFileMapping == NULL) throwIllegalStateException(env, "Failed to find MumbleLink constructor.");
    }

    return (*env)->NewObject(env, cls, cid, instance, buffer, handle);
}

JNIEXPORT void JNICALL Java_com_github_gw2toolbelt_gw2ml_MumbleLink_nClose(JNIEnv* env, jclass clazz, jlong address) {
    UNUSED_PARAM(env);
    UNUSED_PARAM(clazz);

    GW2MLinstance* instance = (GW2MLinstance*) address;

    UnmapViewOfFile(instance->linkedMem);
    CloseHandle(instance->hFileMapping);
    free(instance);
}