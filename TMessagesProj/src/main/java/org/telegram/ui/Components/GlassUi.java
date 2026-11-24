package org.telegram.ui.Components;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.style.ForegroundColorSpan;

public class GlassUi {
    public static final int BLUE = 0xFF1D8CF8;

    public static int dp(float v) {
        return (int) Math.ceil(Resources.getSystem().getDisplayMetrics().density * v);
    }

    public static GradientDrawable glass(float r) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0xF8FFFFFF, 0xB8FFFFFF});
        g.setCornerRadius(dp(r));
        g.setStroke(dp(1), 0xCCFFFFFF);
        return g;
    }

    static TextView label(Context c, CharSequence t, float sp, boolean bold) {
        TextView v = new TextView(c);
        v.setText(t);
        v.setTextSize(sp);
        v.setTextColor(Color.BLACK);
        v.setGravity(Gravity.CENTER);
        v.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        return v;
    }

    public static class Header extends FrameLayout {
        public final TextView edit, title, icon1, icon2;

        public Header(Context c) {
            super(c);
            setBackgroundColor(0xFFE6EAF1);
            setPadding(dp(16), dp(10), dp(16), dp(8));
            edit = label(c, "Edit", 18, false);
            edit.setPadding(dp(20), 0, dp(20), 0);
            edit.setBackground(glass(22));
            edit.setElevation(dp(4));
            addView(edit, new LayoutParams(LayoutParams.WRAP_CONTENT, dp(44), Gravity.START | Gravity.CENTER_VERTICAL));
            SpannableString s = new SpannableString("Chats \u2605");
            s.setSpan(new ForegroundColorSpan(BLUE), 6, 7, 0);
            title = label(c, s, 18, true);
            addView(title, new LayoutParams(LayoutParams.WRAP_CONTENT, dp(44), Gravity.CENTER));
            LinearLayout right = new LinearLayout(c);
            right.setBackground(glass(22));
            right.setElevation(dp(4));
            right.setPadding(dp(6), 0, dp(6), 0);
            icon1 = label(c, "\u270E", 24, false);
            icon2 = label(c, "+", 26, false);
            right.addView(icon1, new LinearLayout.LayoutParams(dp(44), dp(44)));
            right.addView(icon2, new LinearLayout.LayoutParams(dp(44), dp(44)));
            addView(right, new LayoutParams(LayoutParams.WRAP_CONTENT, dp(44), Gravity.END | Gravity.CENTER_VERTICAL));
        }
    }

    public static class Pills extends FrameLayout {
        public interface Listener { void onSelect(int index); }
        public Listener listener;
        private final View ind;
        private final LinearLayout row;
        private final boolean tint;
        private int sel = 0;

        public Pills(Context c, float radius, boolean tint, String... names) {
            super(c);
            this.tint = tint;
            setBackground(glass(radius));
            setElevation(dp(6));
            setPadding(dp(4), dp(4), dp(4), dp(4));
            ind = new View(c);
            GradientDrawable g = new GradientDrawable();
            g.setColor(0x2E787880);
            g.setCornerRadius(dp(radius - 4));
            ind.setBackground(g);
            addView(ind, new LayoutParams(0, LayoutParams.MATCH_PARENT));
            row = new LinearLayout(c);
            addView(row, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            for (int i = 0; i < names.length; i++) {
                final int idx = i;
                TextView t = label(c, names[i], tint ? 11 : 16, false);
                t.setOnClickListener(v -> select(idx));
                row.addView(t, new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
            }
            paint();
        }

        public void select(int i) {
            sel = i;
            paint();
            move(true);
            if (listener != null) listener.onSelect(i);
        }

        private void paint() {
            for (int i = 0; i < row.getChildCount(); i++) {
                ((TextView) row.getChildAt(i)).setTextColor(tint && i == sel ? BLUE : Color.BLACK);
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            post(() -> move(false));
        }

        private void move(boolean anim) {
            View t = row.getChildAt(sel);
            if (t == null || t.getWidth() == 0) return;
            if (ind.getLayoutParams().width != t.getWidth()) {
                ind.getLayoutParams().width = t.getWidth();
                ind.requestLayout();
            }
            float x = t.getLeft();
            if (anim) {
                ind.animate().translationX(x).setDuration(380).setInterpolator(new OvershootInterpolator(1.1f)).start();
            } else {
                ind.setTranslationX(x);
            }
        }
    }

    public static class TabBar extends LinearLayout {
        public final Pills pills;
        public final TextView search;

        public TabBar(Context c) {
            super(c);
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER);
            setPadding(dp(14), 0, dp(14), dp(10));
            pills = new Pills(c, 36, true, "\u263A\nContacts", "\u260E\nCalls", "\u2709\nChats", "\u2699\nSettings");
            pills.select(2);
            addView(pills, new LayoutParams(0, dp(68), 1f));
            search = label(c, "\uD83D\uDD0D", 22, false);
            search.setBackground(glass(29));
            search.setElevation(dp(6));
            LayoutParams lp = new LayoutParams(dp(58), dp(58));
            lp.leftMargin = dp(10);
            addView(search, lp);
        }
    }

    public static void go(org.telegram.ui.ActionBar.BaseFragment from, String cls) {
        try {
            Class<?> c = Class.forName(cls);
            Object o;
            try {
                o = c.getConstructor().newInstance();
            } catch (NoSuchMethodException e) {
                o = c.getConstructor(android.os.Bundle.class).newInstance((android.os.Bundle) null);
            }
            from.presentFragment((org.telegram.ui.ActionBar.BaseFragment) o);
        } catch (Throwable ignore) {
        }
    }

    public static void padLists(android.view.View v, int bottomDp) {
        try {
            if (v instanceof androidx.recyclerview.widget.RecyclerView) {
                androidx.recyclerview.widget.RecyclerView rv = (androidx.recyclerview.widget.RecyclerView) v;
                androidx.recyclerview.widget.RecyclerView.LayoutManager lm = rv.getLayoutManager();
                if (lm instanceof androidx.recyclerview.widget.LinearLayoutManager
                        && ((androidx.recyclerview.widget.LinearLayoutManager) lm).getOrientation() == androidx.recyclerview.widget.LinearLayoutManager.VERTICAL) {
                    rv.setClipToPadding(false);
                    rv.setPadding(rv.getPaddingLeft(), rv.getPaddingTop(), rv.getPaddingRight(), dp(bottomDp));
                }
                return;
            }
            if (v instanceof android.view.ViewGroup) {
                android.view.ViewGroup vg = (android.view.ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    padLists(vg.getChildAt(i), bottomDp);
                }
            }
        } catch (Throwable ignore) {
        }
    }
}
