/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.lenovo.pen.cap.base;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;

/**
 * Gives every Settings style page of PenService (an activity with the ZUI
 * toolbar, id "toolbar") the PixelOS toolbar and the edge to edge layout,
 * also the pages that do not use BasePreferenceFragmentCompat (for example
 * the handwriting recognition languages). Registered once, from the first
 * pen settings page; floating and dialog activities have no such toolbar and
 * are left alone.
 */
final class PageStyler implements Application.ActivityLifecycleCallbacks {
    static boolean sRegistered;

    static void register(Activity activity) {
        if (sRegistered || activity == null) {
            return;
        }
        sRegistered = true;
        activity.getApplication().registerActivityLifecycleCallbacks(new PageStyler());
    }

    /** Styles [activity] once its views are laid out. */
    static void style(final Activity activity) {
        final View decor = activity.getWindow().getDecorView();
        decor.post(new Runnable() {
            @Override
            public void run() {
                int id = activity.getResources().getIdentifier("toolbar", "id",
                        activity.getPackageName());
                View toolbar = id == 0 ? null : activity.findViewById(id);
                if (toolbar == null) {
                    return;
                }
                PixelToolbar.attach(toolbar);
                EdgeToEdge.apply(activity);
            }
        });
    }

    @Override
    public void onActivityPostCreated(Activity activity, Bundle savedInstanceState) {
        style(activity);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {}

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}
}
