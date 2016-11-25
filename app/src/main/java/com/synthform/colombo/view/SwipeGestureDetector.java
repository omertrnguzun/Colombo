package com.synthform.colombo.view;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.HashMap;

/**
 * Created by riccardobusetti on 25/11/16.
 */

public class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {

    private Context context;

    public SwipeGestureDetector(Context context) {
        this.context = context;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            Values values = new Values(context);
            HashMap<String, Integer> valuesForSwipe = values
                    .getValuesForSwipe();
            if (Math.abs(e1.getY() - e2.getY()) > valuesForSwipe.get("swipeMaxOffPath"))
                return false;
            if (e1.getX() - e2.getX() > valuesForSwipe.get("swipeMinDistance")
                    && Math.abs(velocityX) > valuesForSwipe.get("swipeThresholdVelocity")) {
                Toast.makeText(context, "Left Swipe",
                        Toast.LENGTH_SHORT).show();
                return true;
            } else if (e2.getX() - e1.getX() > valuesForSwipe.get("swipeMinDistance")
                    && Math.abs(velocityX) > valuesForSwipe.get("swipeThresholdVelocity")) {
                Toast.makeText(context, "Right Swipe",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        Log.d("tag", "onDown: " + event.toString());
        return true;
    }

}
