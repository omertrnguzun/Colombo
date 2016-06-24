package com.riccardobusetti.colombo.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.riccardobusetti.colombo.R;

public class ObservableWebView extends WebView {

    private final int[] scrollOffset = new int[2];
    private final int[] scrollConsumed = new int[2];
    private int nestedOffsetY;
    private NestedScrollingChildHelper childHelper;

    private float startY, startX, endX;

    private boolean canScrollVertically;

    private CustomWebChromeClient webChromeClient;
    private View previous, next;

    private SharedPreferences prefs;

    public ObservableWebView(Context context) {
        super(context);
        init();
    }

    public ObservableWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ObservableWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        childHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
    }

    public void setNavigationViews(View previous, View next) {
        this.previous = previous;
        this.next = next;
    }

    public void setCanScrollVertically(boolean canScrollVertically) {
        this.canScrollVertically = canScrollVertically;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        getRootView().findViewById(R.id.swipe_layout).setEnabled(getScrollY() == 0 && !canScrollVertically);
    }

    @SuppressWarnings("unused")
    public boolean isVideoFullscreen() {
        return webChromeClient != null && webChromeClient.isVideoFullscreen();
    }

    @Override @SuppressLint("SetJavaScriptEnabled")
    public void setWebChromeClient(WebChromeClient client) {
        if (client instanceof CustomWebChromeClient)
            this.webChromeClient = (CustomWebChromeClient) client;
        super.setWebChromeClient(client);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        MotionEvent event = MotionEvent.obtain(ev);
        final int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            nestedOffsetY = 0;
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        float eventY = event.getY();
        event.offsetLocation(0, nestedOffsetY);

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                float deltaY = startY - eventY;

                if (dispatchNestedPreScroll(0, (int) deltaY, scrollConsumed, scrollOffset)) {
                    deltaY -= scrollConsumed[1];
                    startY = eventY - scrollOffset[1];
                    event.offsetLocation(0, -scrollOffset[1]);
                    nestedOffsetY += scrollOffset[1];
                }

                if (dispatchNestedScroll(0, scrollOffset[1], 0, (int) deltaY, scrollOffset)) {
                    event.offsetLocation(0, scrollOffset[1]);
                    nestedOffsetY += scrollOffset[1];
                    startY -= scrollOffset[1];
                }

                if (prefs.getBoolean("gestures", true)) {
                    if (Math.abs(deltaY) < Math.abs(startX - event.getX())) {
                        float scrollX = startX - event.getX();
                        if ((canGoForward() && scrollX > 0) || (canGoBack() && scrollX < 0))
                            setX(-scrollX / 5);

                        //getRootView().findViewById(R.id.next).setPressed(false);
                        //getRootView().findViewById(R.id.previous).setPressed(false);
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                startY = eventY;
                startX = event.getX();
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (next != null) next.setPressed(false);
                if (previous != null) previous.setPressed(false);

                endX = event.getX();
                animate().x(0).setDuration(50).start();

                if (prefs.getBoolean("gestures", true)) {
                    if (startX - endX > 500 && canGoForward()) goForward();
                    else if (startX - endX < -500 && canGoBack()) goBack();
                }

                stopNestedScroll();
                break;
        }

        return super.onTouchEvent(event);
    }

    /*private int getOverscrollDistance() {
        if (next != null) return next.getWidth();
        else return 200;
    }*/

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        childHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return childHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return childHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        childHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return childHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return childHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return childHelper.dispatchNestedPreFling(velocityX, velocityY);
    }
}