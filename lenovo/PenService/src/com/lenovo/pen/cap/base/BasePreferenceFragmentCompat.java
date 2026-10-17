/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.lenovo.pen.cap.base;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceGroupAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.lenovo.pen.cap.util.ZuiVersions;

/**
 * Replacement of the stock com.lenovo.pen.cap.base.BasePreferenceFragmentCompat
 * (BasePreferenceFragmentCompat.kt), the base class of every pen settings page.
 *
 * Stock behaviour is kept (list side padding from pen_pref_padding[_zui14_5],
 * no scroll bar). On top of it the rows get the PixelOS Settings look
 * (SettingsLib expressive, settingslib_round_background_*): consecutive rows
 * between two categories form one group of surface-bright cards, 20dp corners
 * on the outside of the group, 4dp inside, 2dp apart. Categories, the stock
 * bottom spacer rows and the "Learn more" footer stay outside the cards, on
 * the page background, and end a group; a category without a title leaves the
 * 16dp gap of an untitled Settings section. The backgrounds are set per
 * adapter position from an item decoration, so rows hidden or shown later
 * regroup on the next layout. Row titles, summaries and category titles get
 * the SettingsLib expressive fonts (the stock sizes already match), and the
 * toolbar the Settings back button and title (PixelToolbar).
 */
public class BasePreferenceFragmentCompat extends zui.appcompat.preference.PreferenceFragmentCompat {

    private static final int OUTER_RADIUS_DP = 20;
    private static final int INNER_RADIUS_DP = 4;
    private static final int GAP_DP = 2;
    // The empty category row is about 6dp high; together 16dp between groups.
    private static final int UNTITLED_GROUP_GAP_DP = 10;
    // SettingsLib expressive rows: at least 72dp, 16dp vertical padding
    // (list_text_vertical_padding_zui in PenServiceResTarget).
    private static final int ROW_MIN_HEIGHT_DP = 72;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        RecyclerView list = getListView();
        Context context = list.getContext();
        Resources res = context.getResources();
        int padding = (int) res.getDimension(res.getIdentifier(
                ZuiVersions.isZui14_5OrLater() ? "pen_pref_padding_zui14_5" : "pen_pref_padding",
                "dimen", context.getPackageName()));
        list.setPadding(padding, list.getPaddingTop(), padding, list.getPaddingBottom());
        list.setVerticalScrollBarEnabled(false);
        // The cards replace the ZUI dividers between rows.
        setDivider(null);
        list.addItemDecoration(new CardDecoration(context));
        // After the activity attached the page, the toolbar is in the window.
        list.post(new Runnable() {
            @Override
            public void run() {
                PixelToolbar.attach(list);
                Activity activity = EdgeToEdge.activity(list.getContext());
                if (activity != null) {
                    PageStyler.register(activity);
                    EdgeToEdge.apply(activity);
                    // A list added after the first insets pass gets them too.
                    list.requestApplyInsets();
                }
                mergeGroupNotes(getPreferenceScreen());
            }
        });
        return view;
    }

    /**
     * A stock note row right below a group (FooterPreference, for example the
     * handwriting page's "other recognition is not affected" line) becomes the
     * summary of the group's last row, like a Settings row summary, instead of
     * grey text under the cards. A note at the top of a page stays a heading.
     */
    static void mergeGroupNotes(PreferenceGroup screen) {
        if (screen == null) {
            return;
        }
        Preference previous = null;
        for (int i = 0; i < screen.getPreferenceCount(); i++) {
            Preference preference = screen.getPreference(i);
            if (!preference.isVisible()) {
                continue;
            }
            if (CardDecoration.isDescription(preference) && previous instanceof PreferenceGroup
                    && !TextUtils.isEmpty(preference.getTitle())
                    // A format template (the footer fills in its arguments when
                    // bound) cannot become a summary: ListPreference.getSummary
                    // runs String.format on it and throws on %2$s.
                    && preference.getTitle().toString().indexOf('%') < 0) {
                Preference last = lastVisible((PreferenceGroup) previous);
                if (last != null && !(last instanceof PreferenceGroup)
                        && !CardDecoration.isDescription(last)
                        && TextUtils.isEmpty(last.getSummary())) {
                    if (last instanceof zui.appcompat.preference.PreferenceWithArrow) {
                        // Its summary sits right of the title (a value) by
                        // default; a description goes below the title.
                        ((zui.appcompat.preference.PreferenceWithArrow) last).setSummaryLocation(
                                zui.appcompat.preference.PreferenceWithArrow.LOCATION_BOTTOM);
                        int bottom = last.getContext().getResources().getIdentifier(
                                "preference_appcompat_zui", "layout",
                                last.getContext().getPackageName());
                        if (bottom != 0) {
                            last.setLayoutResource(bottom);
                        }
                    }
                    last.setSummary(preference.getTitle());
                    // Hiding the note changes the hierarchy, so the adapter
                    // also picks up the new row layout.
                    preference.setVisible(false);
                    continue;
                }
            }
            previous = preference;
        }
    }

    static Preference lastVisible(PreferenceGroup group) {
        for (int i = group.getPreferenceCount() - 1; i >= 0; i--) {
            Preference preference = group.getPreference(i);
            if (preference.isVisible()) {
                return preference;
            }
        }
        return null;
    }

    // Not private: AppListCards (the app selection pages) uses the same cards.
    static final class CardDecoration extends RecyclerView.ItemDecoration {
        static final String FOOTER_CLASS = "com.lenovo.pen.cap.widget.FooterPreference";
        static final int SINGLE = 0;
        static final int TOP = 1;
        static final int MIDDLE = 2;
        static final int BOTTOM = 3;
        static final int FLAT_TOP = 4;
        static final int FLAT_BOTTOM = 8;

        private final float mOuterRadius;
        private final float mInnerRadius;
        final int mGap;
        private final int mUntitledGroupGap;
        final int mRowMinHeight;
        private final int mCardColor;
        private final ColorStateList mRippleColor;
        private final int mSpacerLayout;
        private final int mFooterLayout;
        private RecyclerView.Adapter mObserved;
        final Typeface mTitleFont = Typeface.create("variable-title-medium",
                Typeface.NORMAL);
        final Typeface mSummaryFont = Typeface.create("variable-body-medium",
                Typeface.NORMAL);
        final Typeface mCategoryFont = Typeface.create(
                "variable-title-small-emphasized", Typeface.NORMAL);
        private final int mTextColor;

        CardDecoration(Context context) {
            Resources res = context.getResources();
            float density = res.getDisplayMetrics().density;
            mOuterRadius = OUTER_RADIUS_DP * density;
            mInnerRadius = INNER_RADIUS_DP * density;
            mGap = Math.round(GAP_DP * density);
            mUntitledGroupGap = Math.round(UNTITLED_GROUP_GAP_DP * density);
            mRowMinHeight = Math.round(ROW_MIN_HEIGHT_DP * density);
            boolean night = (res.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                    == Configuration.UI_MODE_NIGHT_YES;
            mCardColor = context.getColor(night
                    ? android.R.color.system_surface_bright_dark
                    : android.R.color.system_surface_bright_light);
            mTextColor = context.getColor(night
                    ? android.R.color.system_on_surface_dark
                    : android.R.color.system_on_surface_light);
            TypedArray a = context.obtainStyledAttributes(
                    new int[] { android.R.attr.colorControlHighlight });
            ColorStateList ripple = a.getColorStateList(0);
            a.recycle();
            mRippleColor = ripple != null ? ripple : ColorStateList.valueOf(0x1f000000);
            // Empty row at the end of the stock pen pages (bottom padding).
            mSpacerLayout = res.getIdentifier("pen_settings_preference_space", "layout",
                    context.getPackageName());
            // "Learn more" link below the last group.
            mFooterLayout = res.getIdentifier("pen_settings_preference_footer_right", "layout",
                    context.getPackageName());
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                RecyclerView.State state) {
            outRect.setEmpty();
            RecyclerView.Adapter adapter = parent.getAdapter();
            if (!(adapter instanceof PreferenceGroupAdapter)) {
                return;
            }
            observe(parent, adapter);
            PreferenceGroupAdapter prefs = (PreferenceGroupAdapter) adapter;
            int position = parent.getChildAdapterPosition(view);
            Preference preference = item(prefs, position);
            if (preference instanceof PreferenceCategory) {
                font(view, android.R.id.title, mCategoryFont);
            } else if (isDescription(preference)) {
                // Page description (pen gestures): heading text on the page
                // background, like a category title, not a grey card.
                font(view, android.R.id.title, mCategoryFont);
                View title = view.findViewById(android.R.id.title);
                if (title instanceof TextView
                        && ((TextView) title).getCurrentTextColor() != mTextColor) {
                    ((TextView) title).setTextColor(mTextColor);
                }
            } else if (preference != null) {
                font(view, android.R.id.title, mTitleFont);
                font(view, android.R.id.summary, mSummaryFont);
            }
            if (isGroupEdge(preference)) {
                // Rows of the normal layout are recycled for cards too.
                if (view.getBackground() instanceof Card) {
                    view.setBackground(null);
                    view.setMinimumHeight(0);
                }
                if (position > 0 && preference instanceof PreferenceCategory
                        && TextUtils.isEmpty(preference.getTitle())) {
                    outRect.top = mUntitledGroupGap;
                }
                return;
            }
            boolean first = isGroupEdge(item(prefs, position - 1));
            boolean last = isGroupEdge(item(prefs, position + 1));
            if (view.getMinimumHeight() < mRowMinHeight) {
                view.setMinimumHeight(mRowMinHeight);
            }
            int shape = first ? (last ? SINGLE : TOP) : (last ? BOTTOM : MIDDLE);
            // A picture or slider row that belongs to the row above it forms
            // one card with it: no gap and square corners where they meet.
            boolean joinedAbove = !first && isJoinedToAbove(preference);
            boolean joinedBelow = !last && isJoinedToAbove(item(prefs, position + 1));
            if (joinedAbove) {
                shape |= FLAT_TOP;
            }
            if (joinedBelow) {
                shape |= FLAT_BOTTOM;
            }
            if (!first && !joinedAbove) {
                outRect.top = mGap;
            }
            Drawable background = view.getBackground();
            if (!(background instanceof Card) || ((Card) background).mShape != shape) {
                view.setBackground(card(shape));
            }
        }

        /** Regroup when rows are added, removed or hidden. */
        void observe(RecyclerView parent, RecyclerView.Adapter adapter) {
            if (mObserved == adapter) {
                return;
            }
            mObserved = adapter;
            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    invalidate(parent);
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    invalidate(parent);
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    invalidate(parent);
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    invalidate(parent);
                }
            });
        }

        // Not private: called from the adapter observer in observe(). The compat dex is built with d8
        // --no-desugaring, so a private member reached from another class (javac's
        // nestmate access) is not rewritten and ART throws IllegalAccessError.
        static void invalidate(RecyclerView parent) {
            // No lambdas: the compat dex is built without desugaring.
            parent.post(new Runnable() {
                @Override
                public void run() {
                    if (!parent.isComputingLayout()) {
                        parent.invalidateItemDecorations();
                    }
                }
            });
        }

        /** Sets the font before the row is measured; no-op when it already has it. */
        static void font(View row, int id, Typeface font) {
            View text = row.findViewById(id);
            if (text instanceof TextView && ((TextView) text).getTypeface() != font) {
                ((TextView) text).setTypeface(font);
            }
        }

        private static Preference item(PreferenceGroupAdapter adapter, int position) {
            if (position < 0 || position >= adapter.getItemCount()) {
                return null;
            }
            return adapter.getItem(position);
        }

        /** The stock text row above or below a page (FooterPreference). */
        static boolean isDescription(Preference preference) {
            return preference != null && FOOTER_CLASS.equals(preference.getClass().getName());
        }

        private boolean isGroupEdge(Preference preference) {
            if (preference == null || preference instanceof PreferenceCategory
                    || isDescription(preference)) {
                return true;
            }
            int layout = preference.getLayoutResource();
            if ((mSpacerLayout != 0 && layout == mSpacerLayout)
                    || (mFooterLayout != 0 && layout == mFooterLayout)) {
                return true;
            }
            // The spacer rows are bound with the normal row layout (the ZUI
            // Preference replaces the layout from the XML), so recognise them
            // by their content: a plain Preference with nothing to show and
            // nothing to tap. Picture and preview rows (own classes, no title
            // either) keep their cards.
            return preference.getClass() == zui.appcompat.preference.Preference.class
                    && !preference.isSelectable() && TextUtils.isEmpty(preference.getTitle())
                    && TextUtils.isEmpty(preference.getSummary());
        }

        /**
         * Rows that only show what the row above them does (gesture previews)
         * or adjust it (the writing feedback strength under the pen effect).
         */
        static boolean isJoinedToAbove(Preference preference) {
            if (preference == null) {
                return false;
            }
            String name = preference.getClass().getName();
            return name.equals("com.lenovo.pen.bt.widget.TouchFilmTapTwoPreviewPreference")
                    || name.equals("com.lenovo.pen.bt.widget.TouchFilmAutoSavePreviewPreference")
                    || name.equals("com.lenovo.pen.bt.widget.HapticLevelPreference");
        }

        Drawable card(int shape) {
            int base = shape & 3;
            float top = (shape & FLAT_TOP) != 0 ? 0
                    : base == SINGLE || base == TOP ? mOuterRadius : mInnerRadius;
            float bottom = (shape & FLAT_BOTTOM) != 0 ? 0
                    : base == SINGLE || base == BOTTOM ? mOuterRadius : mInnerRadius;
            GradientDrawable card = new GradientDrawable();
            card.setColor(mCardColor);
            card.setCornerRadii(new float[] {
                    top, top, top, top, bottom, bottom, bottom, bottom });
            return new Card(mRippleColor, card, shape);
        }
    }

    /** Card background of one row; remembers its shape to skip rebuilding. */
    static final class Card extends RippleDrawable {
        final int mShape;

        Card(ColorStateList color, Drawable content, int shape) {
            super(color, content, null);
            mShape = shape;
        }
    }
}
