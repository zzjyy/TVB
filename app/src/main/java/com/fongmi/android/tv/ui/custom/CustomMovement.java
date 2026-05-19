package com.fongmi.android.tv.ui.custom;

import android.graphics.RectF;
import android.text.Editable;
import android.text.Layout;
import android.text.NoCopySpan;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.utils.KeyUtil;
import com.fongmi.android.tv.utils.ResUtil;

public class CustomMovement extends ScrollingMovementMethod {

    private static final int CLICK = 1;
    private static final int UP = 2;
    private static final int DOWN = 3;
    private static final Object FROM_BELOW = new NoCopySpan.Concrete();
    private static CustomMovement sInstance;

    public static CustomMovement getInstance() {
        if (sInstance == null) sInstance = new CustomMovement();
        return sInstance;
    }

    public static void bind(TextView view) {
        CharSequence text = view.getText();
        boolean hasLinks = text instanceof Spannable && ((Spannable) text).getSpans(0, text.length(), ClickableSpan.class).length > 0;
        view.setMovementMethod(hasLinks ? CustomMovement.getInstance() : null);
        if (hasLinks) view.setHighlightColor(ResUtil.getColor(R.color.white_30));
    }

    @Override
    public boolean canSelectArbitrarily() {
        return true;
    }

    @Override
    protected boolean handleMovementKey(TextView widget, Spannable buffer, int keyCode, int movementMetaState, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (KeyEvent.metaStateHasNoModifiers(movementMetaState)) {
                    if (KeyUtil.isActionDown(event) && event.getRepeatCount() == 0 && action(CLICK, widget, buffer)) {
                        return true;
                    }
                }
                break;
        }
        return super.handleMovementKey(widget, buffer, keyCode, movementMetaState, event);
    }

    private boolean findFocus(TextView widget, int direction) {
        View view = widget.focusSearch(direction);
        if (view == null) return false;
        view.requestFocus();
        return true;
    }

    @Override
    protected boolean up(TextView widget, Spannable buffer) {
        if (action(UP, widget, buffer)) return true;
        if (findFocus(widget, View.FOCUS_UP)) return true;
        return super.up(widget, buffer);
    }

    @Override
    protected boolean down(TextView widget, Spannable buffer) {
        if (action(DOWN, widget, buffer)) return true;
        if (findFocus(widget, View.FOCUS_DOWN)) return true;
        return super.down(widget, buffer);
    }

    @Override
    protected boolean left(TextView widget, Spannable buffer) {
        action(UP, widget, buffer);
        return true;
    }

    @Override
    protected boolean right(TextView widget, Spannable buffer) {
        action(DOWN, widget, buffer);
        return true;
    }

    private boolean action(int what, TextView widget, Spannable buffer) {
        Layout layout = widget.getLayout();
        if (layout == null) return false;
        int a = Selection.getSelectionStart(buffer);
        int b = Selection.getSelectionEnd(buffer);
        int selStart = Math.min(a, b);
        int selEnd = Math.max(a, b);
        if (selStart < 0 && buffer.getSpanStart(FROM_BELOW) >= 0) selStart = selEnd = buffer.length();
        if (what == CLICK) return doClick(buffer, widget, selStart, selEnd);
        int padding = widget.getTotalPaddingTop() + widget.getTotalPaddingBottom();
        int first = layout.getLineStart(layout.getLineForVertical(widget.getScrollY()));
        int last = layout.getLineEnd(layout.getLineForVertical(widget.getScrollY() + widget.getHeight() - padding));
        if (selStart > last) selStart = selEnd = Integer.MAX_VALUE;
        if (selEnd < first) selStart = selEnd = -1;
        ClickableSpan[] candidates = buffer.getSpans(first, last, ClickableSpan.class);
        return what == UP ? doUp(buffer, candidates, selStart, selEnd) : doDown(buffer, candidates, selStart, selEnd);
    }

    private boolean doClick(Spannable buffer, TextView widget, int selStart, int selEnd) {
        if (selStart == selEnd) return false;
        ClickableSpan[] links = buffer.getSpans(selStart, selEnd, ClickableSpan.class);
        if (links.length != 1) return false;
        links[0].onClick(widget);
        return true;
    }

    private boolean doUp(Spannable buffer, ClickableSpan[] candidates, int selStart, int selEnd) {
        int bestStart = -1, bestEnd = -1;
        for (ClickableSpan candidate : candidates) {
            int end = buffer.getSpanEnd(candidate);
            if ((end < selEnd || selStart == selEnd) && end > bestEnd) {
                bestStart = buffer.getSpanStart(candidate);
                bestEnd = end;
            }
        }
        if (bestStart < 0) return false;
        Selection.setSelection(buffer, bestEnd, bestStart);
        return true;
    }

    private boolean doDown(Spannable buffer, ClickableSpan[] candidates, int selStart, int selEnd) {
        int bestStart = Integer.MAX_VALUE, bestEnd = Integer.MAX_VALUE;
        for (ClickableSpan candidate : candidates) {
            int start = buffer.getSpanStart(candidate);
            if ((start > selStart || selStart == selEnd) && start < bestStart) {
                bestStart = start;
                bestEnd = buffer.getSpanEnd(candidate);
            }
        }
        if (bestEnd == Integer.MAX_VALUE) return false;
        Selection.setSelection(buffer, bestStart, bestEnd);
        return true;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();
            x += widget.getScrollX();
            y += widget.getScrollY();
            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);
            final RectF touchedLineBounds = new RectF();
            touchedLineBounds.left = layout.getLineLeft(line);
            touchedLineBounds.top = layout.getLineTop(line);
            touchedLineBounds.right = layout.getLineWidth(line) + touchedLineBounds.left;
            touchedLineBounds.bottom = layout.getLineBottom(line);
            if (touchedLineBounds.contains(x, y)) {
                ClickableSpan[] links = buffer.getSpans(off, off, ClickableSpan.class);
                if (links.length != 0) {
                    ClickableSpan link = links[0];
                    if (action == MotionEvent.ACTION_UP) {
                        link.onClick(widget);
                    }
                    return true;
                } else {
                    Selection.removeSelection(buffer);
                }
            }
        }
        if (action == MotionEvent.ACTION_UP) links[0].onClick(widget);
        return true;
    }

    @Override
    public void initialize(TextView widget, Spannable text) {
        Selection.removeSelection(text);
        text.removeSpan(FROM_BELOW);
    }

    @Override
    public void onTakeFocus(TextView view, Spannable text, int dir) {
        Selection.removeSelection(text);
        if (dir == View.FOCUS_BACKWARD) {
            text.setSpan(FROM_BELOW, 0, 0, Spannable.SPAN_POINT_POINT);
        } else {
            text.removeSpan(FROM_BELOW);
        }
    }
}
