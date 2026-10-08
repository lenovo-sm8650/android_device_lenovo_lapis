/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.lenovo.pen.cap.base;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

/**
 * Gives the ZUI toolbar of the pen pages the SettingsLib expressive look: the
 * back button as a 40dp circle (surface container highest) 24dp from the
 * edge, the title in variable-title-large-emphasized (the size stays the
 * stock 22sp) 28dp after it.
 *
 * ZuiAppcompatToolbar clears the back button background once on its first
 * layout, so the look is applied after that layout and again whenever the
 * toolbar lays out with another background on the button (cheap check).
 */
final class PixelToolbar implements Runnable, View.OnLayoutChangeListener {
    static final String TITLE_FONT = "variable-title-large-emphasized";

    final Toolbar mToolbar;
    Drawable mArrow;
    Drawable mClose;
    Drawable mBackground;

    PixelToolbar(Toolbar toolbar) {
        mToolbar = toolbar;
    }

    /** Styles the page toolbar (id "toolbar") of the window [anchor] is in, once. */
    static void attach(View anchor) {
        View root = anchor.getRootView();
        Context context = anchor.getContext();
        int id = context.getResources().getIdentifier("toolbar", "id", context.getPackageName());
        View view = id == 0 ? null : root.findViewById(id);
        if (!(view instanceof Toolbar) || view.getTag(id) != null) {
            return;
        }
        view.setTag(id, Boolean.TRUE);
        PixelToolbar styler = new PixelToolbar((Toolbar) view);
        view.addOnLayoutChangeListener(styler);
        view.post(styler);
    }

    @Override
    public void run() {
        apply();
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom,
            int oldLeft, int oldTop, int oldRight, int oldBottom) {
        ImageButton back = backButton();
        if (back != null && mBackground != null && back.getBackground() != mBackground) {
            v.post(this);
        } else if (mArrow != null && mToolbar.getNavigationIcon() != wantedIcon()) {
            // the page switched its navigation icon (back arrow, "X" while editing)
            v.post(this);
        }
    }

    /** The edit mode of the pen pages replaces the back arrow with an "X". */
    boolean isEditing() {
        Context context = mToolbar.getContext();
        int id = context.getResources().getIdentifier("language_edit_group", "id",
                context.getPackageName());
        View group = id == 0 ? null : mToolbar.getRootView().findViewById(id);
        return group != null && group.getVisibility() == View.VISIBLE;
    }

    Drawable wantedIcon() {
        return isEditing() && mClose != null ? mClose : mArrow;
    }

    void apply() {
        Context context = mToolbar.getContext();
        Resources res = context.getResources();
        float density = res.getDisplayMetrics().density;
        boolean night = (res.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;

        if (mArrow == null) {
            int arrowId = Resources.getSystem().getIdentifier("ic_ab_back_material", "drawable",
                    "android");
            if (arrowId == 0 || mToolbar.getNavigationIcon() == null) {
                return;
            }
            mArrow = context.getDrawable(arrowId).mutate();
            mArrow.setTint(context.getColor(night
                    ? android.R.color.system_on_surface_variant_dark
                    : android.R.color.system_on_surface_variant_light));
            mArrow.setAutoMirrored(true);
            mToolbar.setNavigationIcon(mArrow);
            mBackground = circle(context, night);
            mClose = new CloseDrawable(Math.round(24 * density), Math.round(2 * density),
                    context.getColor(night
                            ? android.R.color.system_on_surface_variant_dark
                            : android.R.color.system_on_surface_variant_light));
        }
        if (mToolbar.getNavigationIcon() != wantedIcon()) {
            mToolbar.setNavigationIcon(wantedIcon());
        }

        ImageButton back = backButton();
        if (back != null) {
            int size = Math.round(40 * density);
            ViewGroup.LayoutParams lp = back.getLayoutParams();
            boolean changed = lp.width != size || lp.height != size;
            lp.width = size;
            lp.height = size;
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) lp).setMarginStart(
                        Math.round(24 * density) - mToolbar.getPaddingStart());
            }
            if (changed) {
                back.setLayoutParams(lp);
            }
            back.setMinimumWidth(0);
            back.setMinimumHeight(0);
            back.setPadding(0, 0, 0, 0);
            back.setScaleType(ImageView.ScaleType.CENTER);
            back.setBackground(mBackground);
        }
        mToolbar.setContentInsetStartWithNavigation(0);
        mToolbar.setTitleMarginStart(Math.round(28 * density));

        Typeface font = Typeface.create(TITLE_FONT, Typeface.NORMAL);
        for (int i = 0; i < mToolbar.getChildCount(); i++) {
            View child = mToolbar.getChildAt(i);
            if (child instanceof TextView) {
                TextView title = (TextView) child;
                if (title.getTypeface() != font) {
                    title.setTypeface(font);
                }
                title.setTextColor(context.getColor(night
                        ? android.R.color.system_on_surface_dark
                        : android.R.color.system_on_surface_light));
            }
        }
    }

    ImageButton backButton() {
        for (int i = 0; i < mToolbar.getChildCount(); i++) {
            View child = mToolbar.getChildAt(i);
            if (child instanceof ImageButton
                    && ((ImageButton) child).getDrawable() == mToolbar.getNavigationIcon()) {
                return (ImageButton) child;
            }
        }
        return null;
    }

    /**
     * The "X" of the back button while editing: 24dp like the back arrow and
     * centered in the button. The stock one (selector_ic_close) is inset by the
     * ZUI action bar padding, which moves it out of the 40dp circle.
     */
    static final class CloseDrawable extends Drawable {
        private final int mSize;
        private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        CloseDrawable(int size, int strokeWidth, int color) {
            mSize = size;
            mPaint.setColor(color);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(strokeWidth);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
        }

        @Override
        public void draw(Canvas canvas) {
            Rect b = getBounds();
            // 12dp of the 24dp box, like ic_close of Material
            float inset = b.width() * 6f / 24f;
            canvas.drawLine(b.left + inset, b.top + inset, b.right - inset, b.bottom - inset,
                    mPaint);
            canvas.drawLine(b.left + inset, b.bottom - inset, b.right - inset, b.top + inset,
                    mPaint);
        }

        @Override
        public int getIntrinsicWidth() {
            return mSize;
        }

        @Override
        public int getIntrinsicHeight() {
            return mSize;
        }

        @Override
        public void setAlpha(int alpha) {
            mPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            mPaint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    /** SettingsLib expressive back button: a filled circle with a ripple. */
    static Drawable circle(Context context, boolean night) {
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(context.getColor(night
                ? android.R.color.system_surface_container_highest_dark
                : android.R.color.system_surface_container_highest_light));
        TypedArray a = context.obtainStyledAttributes(
                new int[] { android.R.attr.colorControlHighlight });
        int ripple = a.getColor(0, 0x1f000000);
        a.recycle();
        GradientDrawable mask = new GradientDrawable();
        mask.setShape(GradientDrawable.OVAL);
        mask.setColor(Color.WHITE);
        return new RippleDrawable(ColorStateList.valueOf(ripple), circle, mask);
    }
}
