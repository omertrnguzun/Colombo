package com.riccardobusetti.colombo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by riccardobusetti on 10/06/16.
 */

public class PreferencesUtil {

    private static SharedPreferences getDefaultSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean isFirstTime(Context context) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean("first_time", true)) {
            sharedPreferences.edit().putBoolean("first_time", false).apply();
            return true;
        }
        return false;
    }


}
