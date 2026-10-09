/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.zui.input.handwriting;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

/** Removes the retired Lenovo IME and migrates existing selections to an enabled keyboard. */
public final class ZuiHandWritingManager {
    private static final String TAG = "LenovoHandwriting";
    private static final String PEN_IME =
            "com.lenovo.penservice/com.lenovo.pen.handwriting.service.HandwritingIme";
    private static final String GBOARD =
            "com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME";
    private static final String HANDWRITING = "pen_set_freewrite_on";
    private static final String LAST_KEYBOARD = "last_keyboard";
    private static ZuiHandWritingManager sInstance;

    final Context mContext;
    final Handler mHandler = new Handler(Looper.getMainLooper());
    final SharedPreferences mPrefs;
    final InputMethodManager mImm;

    public static synchronized ZuiHandWritingManager getInstance(Context context) {
        if (sInstance == null) sInstance = new ZuiHandWritingManager(context);
        return sInstance;
    }

    private ZuiHandWritingManager(Context context) {
        Context app = context.getApplicationContext();
        mContext = app != null ? app : context;
        mPrefs = mContext.createDeviceProtectedStorageContext()
                .getSharedPreferences("handwriting_ime_compat", Context.MODE_PRIVATE);
        mImm = mContext.getSystemService(InputMethodManager.class);
        ContentResolver resolver = mContext.getContentResolver();
        ContentObserver observer = new SettingsObserver(mHandler, this);
        resolver.registerContentObserver(Settings.Secure.getUriFor(
                Settings.Secure.DEFAULT_INPUT_METHOD), false, observer);
        resolver.registerContentObserver(Settings.Secure.getUriFor(
                Settings.Secure.ENABLED_INPUT_METHODS), false, observer);
        resolver.registerContentObserver(Settings.Secure.getUriFor(HANDWRITING), false, observer);
        // Run after provider/Application startup, including stock initLenovoIME().
        mHandler.post(new Refresh(this));
    }

    // Retained for stock callers, although the Lenovo IME is no longer registered.
    public void setHwEnable(int enabled) { mHandler.post(new Refresh(this)); }
    public void setToolType(int toolType) {}
    public void registerIMEClient(IIMEClient client) {}
    public void unregisterIMEClient(IIMEClient client) {}

    void refresh() {
        try {
            ContentResolver resolver = mContext.getContentResolver();
            String current = Settings.Secure.getString(resolver,
                    Settings.Secure.DEFAULT_INPUT_METHOD);
            if (PEN_IME.equals(current)) {
                restoreKeyboard("retired Lenovo IME");
            } else if (isKeyboard(current, mImm.getEnabledInputMethodList())) {
                mPrefs.edit().putString(LAST_KEYBOARD, current).apply();
            }
            if (Settings.Secure.getInt(resolver, HANDWRITING, 0) != 0) {
                Settings.Secure.putInt(resolver, HANDWRITING, 0);
            } else if (Settings.Secure.getString(resolver, HANDWRITING) == null) {
                Settings.Secure.putInt(resolver, HANDWRITING, 0);
            }
            // Stock Application.onCreate() adds this ID even without a manifest service.
            // Remove only that entry, retaining all other IMEs and their subtype suffixes.
            String enabled = Settings.Secure.getString(resolver,
                    Settings.Secure.ENABLED_INPUT_METHODS);
            if (enabled != null) {
                StringBuilder kept = new StringBuilder();
                boolean removed = false;
                for (String entry : enabled.split(":")) {
                    String id = entry.split(";", 2)[0];
                    if (PEN_IME.equals(id)) {
                        removed = true;
                    } else {
                        if (kept.length() > 0) kept.append(':');
                        kept.append(entry);
                    }
                }
                if (removed) Settings.Secure.putString(resolver,
                        Settings.Secure.ENABLED_INPUT_METHODS, kept.toString());
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Cannot migrate retired handwriting IME", e);
        }
    }

    static boolean isKeyboard(String id, List<InputMethodInfo> enabled) {
        if (id == null || PEN_IME.equals(id)) return false;
        for (InputMethodInfo info : enabled) {
            if (id.equals(info.getId()) && !info.isAuxiliaryIme()) return true;
        }
        return false;
    }

    String chooseKeyboard(List<InputMethodInfo> enabled) {
        String previous = mPrefs.getString(LAST_KEYBOARD, null);
        if (isKeyboard(previous, enabled)) return previous;
        previous = Settings.Global.getString(mContext.getContentResolver(), "bk_ime");
        if (isKeyboard(previous, enabled)) return previous;
        if (isKeyboard(GBOARD, enabled)) return GBOARD;
        for (InputMethodInfo info : enabled) {
            if (isKeyboard(info.getId(), enabled)) return info.getId();
        }
        return null;
    }

    void restoreKeyboard(String reason) {
        try {
            ContentResolver resolver = mContext.getContentResolver();
            if (!PEN_IME.equals(Settings.Secure.getString(resolver,
                    Settings.Secure.DEFAULT_INPUT_METHOD))) return;
            String target = chooseKeyboard(mImm.getEnabledInputMethodList());
            if (target == null) {
                mImm.showInputMethodPicker();
                Log.w(TAG, "No enabled keyboard for Lenovo IME migration");
                return;
            }
            // PenService runs under uid 1000, for which setInputMethod(null, id)
            // deliberately does nothing. Use the same setting as ZUI restoreIme().
            Settings.Secure.putInt(resolver, Settings.Secure.SELECTED_INPUT_METHOD_SUBTYPE, -1);
            if (Settings.Secure.putString(resolver, Settings.Secure.DEFAULT_INPUT_METHOD, target)) {
                Log.i(TAG, "Restored keyboard: " + target + " (" + reason + ")");
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Cannot restore keyboard", e);
        }
    }

    static final class SettingsObserver extends ContentObserver {
        final ZuiHandWritingManager owner;
        SettingsObserver(Handler handler, ZuiHandWritingManager owner) {
            super(handler);
            this.owner = owner;
        }
        @Override public void onChange(boolean selfChange) { owner.refresh(); }
    }

    static final class Refresh implements Runnable {
        final ZuiHandWritingManager owner;
        Refresh(ZuiHandWritingManager owner) { this.owner = owner; }
        @Override public void run() { owner.refresh(); }
    }

}
