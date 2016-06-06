package com.riccardobusetti.colombo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.riccardobusetti.colombo.R;

public class ObservableWebView extends WebView {

    private GestureDetector gestureDetector;

    private boolean flag;

    public ObservableWebView(Context context) {
        super(context);
    }

    public ObservableWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (flag) {
            flag = false;
            return;
        }

        View searchBar = getRootView().findViewById(R.id.floating_search_view);
        if (searchBar != null) {
            int deltaT = oldt - t;

            float oldY = searchBar.getTranslationY();
            float newY = Math.max(-searchBar.getHeight(), Math.min(0, searchBar.getTranslationY() + deltaT));

            searchBar.setTranslationY(newY);

            View card = getRootView().findViewById(R.id.card);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) card.getLayoutParams();
            layoutParams.topMargin = dpToPx(0);
            card.setLayoutParams(layoutParams);

            card.setTranslationY(searchBar.getHeight() + newY);

            flag = true;
            if ((int) (newY - oldY) != 0) {
                scrollBy(0, deltaT);
            }
        }

        View swipe = getRootView().findViewById(R.id.swipe_layout);

        int height = (int) Math.floor(this.getContentHeight() * this.getScale());
        int webViewHeight = this.getMeasuredHeight();
        if(this.getScrollY() + webViewHeight < height){
            swipe.setEnabled(true);
        } else {
            swipe.setEnabled(false);
        }
    }

    private int dpToPx(int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, getResources().getDisplayMetrics());
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return gestureDetector.onTouchEvent(ev) || super.onTouchEvent(ev);
    }

    public void setGestureDetector(GestureDetector gestureDetector) {
        this.gestureDetector = gestureDetector;
    }
}