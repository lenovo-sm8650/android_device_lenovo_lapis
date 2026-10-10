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
            Intent target = new Intent().setClassName(PACKAGE, activity);
            // The Settings trampoline starts asynchronously, so check the target
            // before launching it to preserve the fallback for older Gboard versions.
            if (target.resolveActivity(context.getPackageManager()) == null) {
                continue;
            }
            try {
                if (openEmbedded(context, target)) return;
                context.startActivity(target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return;
            } catch (ActivityNotFoundException | SecurityException e) {
                Log.w("LenovoHandwriting", "Gboard settings unavailable: " + activity);
            }
        }
        Intent target = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
        if (!openEmbedded(context, target)) {
            context.startActivity(target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    private static boolean openEmbedded(Context context, Intent target) {
        Intent embedded = new Intent(Settings.ACTION_SETTINGS_EMBED_DEEP_LINK_ACTIVITY)
                .setPackage("com.android.settings")
                .putExtra(Settings.EXTRA_SETTINGS_EMBEDDED_DEEP_LINK_INTENT_URI,
                        target.toUri(Intent.URI_INTENT_SCHEME))
                .putExtra(Settings.EXTRA_SETTINGS_EMBEDDED_DEEP_LINK_HIGHLIGHT_MENU_KEY,
                        "top_level_system")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (embedded.resolveActivity(context.getPackageManager()) == null) {
            return false;
        }
        try {
            context.startActivity(embedded);
            return true;
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.w("LenovoHandwriting", "Settings embedding unavailable", e);
            return false;
        }
    }
}
