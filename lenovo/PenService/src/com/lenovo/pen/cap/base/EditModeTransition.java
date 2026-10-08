/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.lenovo.pen.cap.base;

import android.app.Activity;
import android.graphics.Rect;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.PathInterpolator;

/**
 * Window callback of the recognition languages page that starts a transition
 * of the page before a tap on the toolbar or the delete button, or the back
 * key, is handled: those switch between the lists and the edit list, by
 * changing the visibility of the views of two groups with no animation.
 */
final class EditModeTransition implements Window.Callback {
    private final Activity mActivity;
    private final ViewGroup mContainer;
    private final Window.Callback mBase;

    EditModeTransition(Activity activity, ViewGroup container, Window.Callback base) {
        mActivity = activity;
        mContainer = container;
        mBase = base;
    }

    /** Hooks the page, once. */
    static void install(Activity activity) {
        Window window = activity.getWindow();
        if (window.getCallback() instanceof EditModeTransition) {
            return;
        }
        int id = activity.getResources().getIdentifier("language_container", "id",
                activity.getPackageName());
        View container = id == 0 ? null : activity.findViewById(id);
        if (!(container instanceof ViewGroup)) {
            return;
        }
        window.setCallback(new EditModeTransition(activity, (ViewGroup) container,
                window.getCallback()));
    }

    private View find(String name) {
        int id = mActivity.getResources().getIdentifier(name, "id", mActivity.getPackageName());
        return id == 0 ? null : mActivity.findViewById(id);
    }

    private boolean isOn(View view, MotionEvent event) {
        if (view == null || view.getVisibility() != View.VISIBLE) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        Rect bounds = new Rect(location[0], location[1], location[0] + view.getWidth(),
                location[1] + view.getHeight());
        return bounds.contains((int) event.getRawX(), (int) event.getRawY());
    }

    private void start() {
        Transition transition = new TransitionSet()
                .setOrdering(TransitionSet.ORDERING_TOGETHER)
                .addTransition(new Fade())
                .addTransition(new ChangeBounds())
                .setDuration(300)
                .setInterpolator(new PathInterpolator(0.2f, 0f, 0f, 1f));
        TransitionManager.beginDelayedTransition(mContainer, transition);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP
                && (isOn(find("toolbar"), event) || isOn(find("language_edit_btn"), event))) {
            start();
        }
        return mBase.dispatchTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        View edit = find("language_edit_list");
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_UP
                && edit != null && edit.getVisibility() == View.VISIBLE) {
            start();
        }
        return mBase.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return mBase.dispatchKeyShortcutEvent(event);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        return mBase.dispatchTrackballEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        return mBase.dispatchGenericMotionEvent(event);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return mBase.dispatchPopulateAccessibilityEvent(event);
    }

    @Override
    public View onCreatePanelView(int featureId) {
        return mBase.onCreatePanelView(featureId);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return mBase.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        return mBase.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return mBase.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return mBase.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
        mBase.onWindowAttributesChanged(attrs);
    }

    @Override
    public void onContentChanged() {
        mBase.onContentChanged();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        mBase.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onAttachedToWindow() {
        mBase.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        mBase.onDetachedFromWindow();
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        mBase.onPanelClosed(featureId, menu);
    }

    @Override
    public boolean onSearchRequested() {
        return mBase.onSearchRequested();
    }

    @Override
    public boolean onSearchRequested(SearchEvent searchEvent) {
        return mBase.onSearchRequested(searchEvent);
    }

    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        return mBase.onWindowStartingActionMode(callback);
    }

    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        return mBase.onWindowStartingActionMode(callback, type);
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        mBase.onActionModeStarted(mode);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        mBase.onActionModeFinished(mode);
    }
}
