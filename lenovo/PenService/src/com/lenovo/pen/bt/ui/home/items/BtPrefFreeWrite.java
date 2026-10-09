/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.lenovo.pen.bt.ui.home.items;

import com.lenovo.pen.cap.ui.SettingsFragment;
import com.lenovo.pen.cap.ui.items.PrefFreeWrite;

/** Bluetooth and capacitive pen settings share the same Gboard destination. */
public final class BtPrefFreeWrite extends PrefFreeWrite {
    public BtPrefFreeWrite(SettingsFragment fragment) { super(fragment); }
}
