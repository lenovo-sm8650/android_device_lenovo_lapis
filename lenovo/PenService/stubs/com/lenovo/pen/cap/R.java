/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.lenovo.pen.cap;

/** Compile-time stand-in; the real class comes from the PenService dex. */
public final class R {
    private R() {}

    public static final class bool {
        // not final, so the compat dex reads the field instead of inlining a made-up id
        public static int zui_config_show_top_level_tether;
    }
}
