/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.lenovo.pen.cap.util;

/** Compile-time stand-in; the real class comes from PenService's own dex. */
public final class SystemProps {
    private SystemProps() {}

    public static boolean isRow() {
        throw new RuntimeException("stub");
    }
}
