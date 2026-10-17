/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.lenovo.pen.common;

/** Compile-time stand-in; the real class comes from PenService's own dex. */
public final class Logger {
    private Logger() {}

    public static void d$default(String msg, boolean flag, int mask, Object marker) {
        throw new RuntimeException("stub");
    }

    public static void e$default(String msg, boolean flag, int mask, Object marker) {
        throw new RuntimeException("stub");
    }
}
