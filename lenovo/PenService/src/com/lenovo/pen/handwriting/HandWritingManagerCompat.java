/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.lenovo.pen.handwriting;

import com.zui.input.handwriting.ZuiHandWritingManager;

import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/**
 * Replaces the lazy initializer of LenovoHandwritingApp.mZuiHandWritingManager.
 * Stock asks getSystemService("zui_hand_writing"), a ZUI-only service, and
 * throws on null, which crashed PenService from the Lenovo handwriting
 * keyboard. Use the local bridge that restores the user's keyboard.
 * The class name must match the Kotlin-generated one exactly.
 */
final class LenovoHandwritingApp$mZuiHandWritingManager$2 extends Lambda<ZuiHandWritingManager>
        implements Function0<ZuiHandWritingManager> {
    private final LenovoHandwritingApp mApp;

    LenovoHandwritingApp$mZuiHandWritingManager$2(LenovoHandwritingApp app) {
        super(0);
        mApp = app;
    }

    @Override
    public final ZuiHandWritingManager invoke() {
        return ZuiHandWritingManager.getInstance(mApp);
    }
}
