/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.lenovo.pen.bt.ui.freewrite.items;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;

import androidx.preference.Preference;

import com.lenovo.pen.bt.ui.freewrite.FreeWriteFragment;

/** Compile-time stand-in; the real class comes from the PenService dex. */
public abstract class PrefFreeItem {
    public PrefFreeItem(FreeWriteFragment fragment) {}

    public final Context getContext() { throw new RuntimeException("stub"); }
    public final void setContext(Context context) {}
    public final FreeWriteFragment getFragment() { throw new RuntimeException("stub"); }
    public final Handler getHandler() { throw new RuntimeException("stub"); }
    public final Preference getPref() { throw new RuntimeException("stub"); }
    public final Resources getResources() { throw new RuntimeException("stub"); }
    public final String getString(int resId) { throw new RuntimeException("stub"); }
    public final void setEnabled(boolean enabled) {}
    public final void setVisible(boolean visible) {}

    public abstract int getKeyResId();
    public abstract void update();

    public void registerReceivers() {}
    public void unregisterReceivers() {}
}
