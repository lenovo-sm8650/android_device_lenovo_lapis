/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.lenovo.pen.cap.base;

import android.content.Context;

/**
 * Replacement of the stock Kotlin interface com.lenovo.pen.cap.base.SettingsManager.
 *
 * BtSettingsManager and CapSettingsManager run DefaultImpls.init from
 * PenIMEApp.initPenConfigs() when the persistent PenService process starts.
 * Besides the stock initDefault + migrate, it turns the quick note switches
 * off whenever ZUI Notes is missing (see ZuiNotesQuickNotes).
 */
public interface SettingsManager {
    void init(Context context);

    void initDefault(Context context);

    void migrate(Context context);

    final class DefaultImpls {
        public static void init(SettingsManager settingsManager, Context context) {
            settingsManager.initDefault(context);
            settingsManager.migrate(context);
            ZuiNotesQuickNotes.track(context);
        }
    }
}
