/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.lenovo.pen.handwriting;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

/** Links to Gboard without changing or depending on its private implementation. */
public final class GboardHandwritingSettings {
    private static final String PACKAGE = "com.google.android.inputmethod.latin";
    private GboardHandwritingSettings() {}

    public static void open(Context context) {
        for (String activity : new String[] {
                "com.google.android.apps.inputmethod.latin.stylus.StylusSettingsActivity",
                "com.google.android.apps.inputmethod.latin.preference.SettingsActivity"}) {
            try {
                context.startActivity(new Intent().setClassName(PACKAGE, activity)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return;
            } catch (ActivityNotFoundException | SecurityException e) {
                Log.w("LenovoHandwriting", "Gboard settings unavailable: " + activity);
            }
        }
        context.startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
