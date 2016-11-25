package com.synthform.colombo.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.ViewConfiguration;

import java.util.HashMap;

/**
 * Created by riccardobusetti on 25/11/16.
 */

public final class Values {

    Context context;

    Values(Context callingContext) {
        context = callingContext;
    }

    /**
     * Get optimal parameter values to determine a swipe on a View.
     *
     * @return HashMap<String, Integer>
     */
    HashMap<String, Integer> getValuesForSwipe() {
        HashMap<String, Integer> swipeValues = new HashMap<String, Integer>();
        ViewConfiguration vc = ViewConfiguration.get(context);
        swipeValues.put("swipeMinDistance", vc.getScaledPagingTouchSlop());
        swipeValues.put("swipeThresholdVelocity",
                vc.getScaledMinimumFlingVelocity());
        swipeValues.put("swipeMaxOffPath", vc.getScaledMinimumFlingVelocity());
        return swipeValues;
    }
}
