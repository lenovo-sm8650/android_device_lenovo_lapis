/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.lenovo.pen.bt.ui.freewrite.items;

import com.lenovo.pen.bt.R;
import com.lenovo.pen.bt.ui.freewrite.FreeWriteFragment;

/** Hides the language-pack row: packs are not downloadable and LanguageActivity is removed. */
public final class PrefLanguage extends PrefFreeItem {
    public PrefLanguage(FreeWriteFragment fragment) {
        super(fragment);
        setVisible(false);
    }

    @Override public int getKeyResId() { return R.string.pref_key_language; }
    @Override public void registerReceivers() {}
    @Override public void unregisterReceivers() {}
    @Override public void update() {}
}
