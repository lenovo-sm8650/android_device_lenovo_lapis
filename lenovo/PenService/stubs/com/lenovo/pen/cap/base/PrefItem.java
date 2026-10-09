/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.lenovo.pen.cap.base;
import android.content.Context;
import android.content.res.Resources;
import androidx.preference.Preference;
import com.lenovo.pen.cap.ui.SettingsFragment;
/** Compile-time stand-in; provided by the stock APK. */
public abstract class PrefItem {
    public PrefItem(SettingsFragment fragment) {}
    public abstract int getKeyResId();
    public abstract void update();
    public final Context getContext() { throw new RuntimeException("stub"); }
    public final Resources getResources() { throw new RuntimeException("stub"); }
    public final Preference getPref() { throw new RuntimeException("stub"); }
}
