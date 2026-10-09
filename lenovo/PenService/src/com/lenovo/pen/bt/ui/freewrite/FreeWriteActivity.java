/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.lenovo.pen.bt.ui.freewrite;

import android.app.Activity;
import android.os.Bundle;

import com.lenovo.pen.handwriting.GboardHandwritingSettings;

/** Redirect old shortcuts/search results as well as the main handwriting preference. */
public final class FreeWriteActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GboardHandwritingSettings.open(this);
        finish();
    }
}
