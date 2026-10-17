/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.lenovo.pen.bt.ui;

import android.app.Activity;
import android.os.Bundle;

/**
 * The stock dialog's only action is SHOW_CTA_DIALOG with a LANGUAGE_KEY extra,
 * which opens the language-pack consent and starts the download. Language-pack
 * downloads are disabled on this ROM and nothing calls DialogActivity.Companion,
 * so this entry finishes at once and asks for no consent.
 */
public final class DialogActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
    }
}
