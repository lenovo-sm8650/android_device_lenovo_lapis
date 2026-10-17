/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.lenovo.pen.cap.util;

import android.content.Context;

/** Compile-time stand-in; the real class comes from the PenService dex. */
public final class ZuiConfig {
    private ZuiConfig() {}

    public static boolean getBoolean(Context context, int resId) {
        throw new RuntimeException("stub");
    }
}
