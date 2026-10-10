/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.lenovo.pen.cap.ui.items;

import androidx.preference.Preference;
import com.lenovo.pen.cap.base.PrefItem;
import com.lenovo.pen.cap.ui.SettingsFragment;
import com.lenovo.pen.handwriting.GboardHandwritingSettings;

/** Keeps the translated handwriting title and opens Gboard without a row summary. */
public class PrefFreeWrite extends PrefItem implements Preference.OnPreferenceClickListener {
    public PrefFreeWrite(SettingsFragment fragment) {
        super(fragment);
        getPref().setOnPreferenceClickListener(this);
        update();
    }

    @Override public int getKeyResId() {
        return getResources().getIdentifier("pref_key_free_write", "string",
                getContext().getPackageName());
    }

    @Override public void update() { getPref().setSummary(null); }
    @Override public boolean onPreferenceClick(Preference preference) {
        openWriteActivity();
        return true;
    }
    public void openWriteActivity() { GboardHandwritingSettings.open(getContext()); }
}
