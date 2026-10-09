/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package androidx.startup;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.zui.input.handwriting.ZuiHandWritingManager;

/** Keep stock AndroidX startup and start the IME migration before Application.onCreate(). */
public class InitializationProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        Context context = getContext();
        if (context == null) throw new IllegalStateException("Context cannot be null");
        if (context.getApplicationContext() == null) return true;
        AppInitializer.getInstance(context).discoverAndInitialize();
        try {
            ZuiHandWritingManager.getInstance(context);
        } catch (RuntimeException e) {
            // A bridge failure must not crash this persistent application.
            Log.e("LenovoHandwriting", "Cannot initialize handwriting migration", e);
        }
        return true;
    }

    @Override public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) { throw new IllegalStateException("Not allowed."); }
    @Override public String getType(Uri uri) { throw new IllegalStateException("Not allowed."); }
    @Override public Uri insert(Uri uri, ContentValues values) { throw new IllegalStateException("Not allowed."); }
    @Override public int delete(Uri uri, String selection, String[] args) { throw new IllegalStateException("Not allowed."); }
    @Override public int update(Uri uri, ContentValues values, String selection,
            String[] args) { throw new IllegalStateException("Not allowed."); }
}
