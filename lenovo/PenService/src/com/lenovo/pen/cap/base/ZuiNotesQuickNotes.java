/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.lenovo.pen.cap.base;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PatternMatcher;
import android.provider.Settings;

import com.lenovo.pen.cap.util.Features;

/**
 * The three quick note switches only make sense with ZUI Notes, and their rows
 * are hidden without it (ClickableWhenDisabledSwitchPreference). Turn them off
 * at PenService start and whenever ZUI Notes is removed or disabled, so a later
 * install starts with them off. Installing ZUI Notes changes nothing, so a
 * switch the user turns on afterwards stays on.
 */
final class ZuiNotesQuickNotes {
    static final String ZUI_NOTES = "com.zui.notes";
    // SystemSettings.Keys of the stock PenService.
    static final String[] SECURE_KEYS = {
        "pen_set_quick_create_click_notes",
        "pen_set_quick_create_screenoff_notes",
    };
    static final String GLOBAL_KEY_PARKER = "parker_quick_note";

    static boolean registered;

    private ZuiNotesQuickNotes() {}

    static synchronized void track(Context context) {
        Context app = context.getApplicationContext();
        if (!registered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            filter.addDataScheme("package");
            filter.addDataSchemeSpecificPart(ZUI_NOTES, PatternMatcher.PATTERN_LITERAL);
            app.registerReceiver(new PackageReceiver(), filter, Context.RECEIVER_NOT_EXPORTED);
            registered = true;
        }
        turnOffWithoutZuiNotes(app);
    }

    static void turnOffWithoutZuiNotes(Context context) {
        if (Features.isSupportZuiNotes(context)) {
            return;
        }
        ContentResolver resolver = context.getContentResolver();
        for (String key : SECURE_KEYS) {
            if (Settings.Secure.getInt(resolver, key, 0) != 0) {
                Settings.Secure.putInt(resolver, key, 0);
            }
        }
        if (Settings.Global.getInt(resolver, GLOBAL_KEY_PARKER, 0) != 0) {
            Settings.Global.putInt(resolver, GLOBAL_KEY_PARKER, 0);
        }
    }

    // Not private: the compat dex is built with d8 --no-desugaring, so private
    // members reached across classes (nestmates) would throw IllegalAccessError.
    static final class PackageReceiver extends BroadcastReceiver {
        PackageReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                return;
            }
            turnOffWithoutZuiNotes(context);
        }
    }
}
