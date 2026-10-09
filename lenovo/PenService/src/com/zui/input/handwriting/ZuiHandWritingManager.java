/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.zui.input.handwriting;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * The stock toolbar calls setHwEnable(3) to return to the keyboard. ZUI implements
 * that in system_server; the class bundled in the APK is only an empty API stub.
 * Keep this user's last enabled, non-auxiliary IME and restore it locally instead.
 * No input text or pen/Bluetooth state is observed.
 */
public final class ZuiHandWritingManager implements View.OnClickListener {
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
    final WindowManager mWindowManager;
    TextView mReturnButton;

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
        mWindowManager = mContext.getSystemService(WindowManager.class);
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

    public void setHwEnable(int enabled) {
        if (enabled == 3) mHandler.post(new ReturnKeyboard(this, "stock keyboard request"));
    }

    public void setToolType(int toolType) {
        if (toolType == 1) mHandler.post(new ReturnKeyboard(this, "finger input"));
    }

    // These old cursor-detection callbacks are not called by this stock PenService.
    public void registerIMEClient(IIMEClient client) {}
    public void unregisterIMEClient(IIMEClient client) {}

    void refresh() {
        try {
            String current = Settings.Secure.getString(mContext.getContentResolver(),
                    Settings.Secure.DEFAULT_INPUT_METHOD);
            if (!PEN_IME.equals(current)) {
                hideReturnButton();
                if (isKeyboard(current, mImm.getEnabledInputMethodList())) {
                    mPrefs.edit().putString(LAST_KEYBOARD, current).apply();
                }
                return;
            }
            if (Settings.Secure.getInt(mContext.getContentResolver(), HANDWRITING, 1) == 0) {
                restoreKeyboard("handwriting disabled");
            } else {
                showReturnButton();
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Cannot update keyboard return state", e);
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
                showReturnButton();
                Toast.makeText(mContext, isKorean() ? "사용할 키보드를 먼저 켜 주세요."
                        : "Enable a keyboard first.", Toast.LENGTH_LONG).show();
                mImm.showInputMethodPicker();
                Log.w(TAG, "No enabled keyboard for return request");
                return;
            }
            // PenService runs under uid 1000, for which setInputMethod(null, id)
            // deliberately does nothing. Use the same setting as ZUI restoreIme().
            Settings.Secure.putInt(resolver, Settings.Secure.SELECTED_INPUT_METHOD_SUBTYPE, -1);
            if (Settings.Secure.putString(resolver, Settings.Secure.DEFAULT_INPUT_METHOD, target)) {
                Log.i(TAG, "Restored keyboard: " + target + " (" + reason + ")");
                hideReturnButton();
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Cannot restore keyboard", e);
        }
    }

    @Override
    public void onClick(View view) {
        restoreKeyboard("keyboard return button");
    }

    boolean isKorean() {
        return "ko".equals(mContext.getResources().getConfiguration().getLocales().get(0)
                .getLanguage());
    }

    int dp(int value) {
        return Math.round(value * mContext.getResources().getDisplayMetrics().density);
    }

    void showReturnButton() {
        if (mReturnButton != null) return;
        boolean dark = (mContext.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        TextView button = new TextView(mContext);
        button.setText(isKorean() ? "키보드" : "Keyboard");
        button.setContentDescription(isKorean() ? "키보드로 돌아가기" : "Return to keyboard");
        button.setTextSize(16);
        button.setTextColor(dark ? Color.WHITE : Color.BLACK);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(20), 0, dp(20), 0);
        button.setMinimumHeight(dp(48));
        GradientDrawable background = new GradientDrawable();
        background.setColor(dark ? Color.rgb(48, 48, 48) : Color.rgb(245, 245, 245));
        background.setCornerRadius(dp(24));
        button.setBackground(background);
        button.setElevation(dp(6));
        button.setOnClickListener(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, dp(48),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.x = dp(16);
        params.y = dp(32);
        params.setTitle("Lenovo handwriting keyboard return");
        mWindowManager.addView(button, params);
        mReturnButton = button;
    }

    void hideReturnButton() {
        if (mReturnButton == null) return;
        mWindowManager.removeView(mReturnButton);
        mReturnButton = null;
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

    static final class ReturnKeyboard implements Runnable {
        final ZuiHandWritingManager owner;
        final String reason;
        ReturnKeyboard(ZuiHandWritingManager owner, String reason) {
            this.owner = owner;
            this.reason = reason;
        }
        @Override public void run() { owner.restoreKeyboard(reason); }
    }
}
