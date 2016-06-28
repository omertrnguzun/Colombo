package com.riccardobusetti.colombo.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ImageView;

import com.riccardobusetti.colombo.R;
import com.riccardobusetti.colombo.util.AppStatus;

/**
 * Created by riccardobusetti on 26/06/16.
 */

public class SplashActivity extends Activity {

    private static final int SPLASH_DISPLAY_LENGTH = 600;
    private static final int SPLASH_IMAGE_LENGHT = 400;
    private ImageView icon, text;
    private CoordinatorLayout coordi;
    private SharedPreferences prefs;

    public static void setLocked(ImageView v) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        v.setColorFilter(cf);
    }

    public static void setUnlocked(ImageView v) {
        v.setColorFilter(null);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);

        coordi = (CoordinatorLayout) findViewById(R.id.coordi_splash);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (AppStatus.getInstance(SplashActivity.this).isOnline()) {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    if (prefs.getBoolean("first_time", true)) {
                        startActivity(new Intent(SplashActivity.this, MainIntroActivity.class));
                        prefs.edit().putBoolean("first_time", false).apply();
                    }
                } else {
                    Snackbar snackbar = Snackbar.make(coordi, R.string.no_connection, Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("PROCEED", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            if (prefs.getBoolean("first_time", true)) {
                                startActivity(new Intent(SplashActivity.this, MainIntroActivity.class));
                                prefs.edit().putBoolean("first_time", false).apply();
                            }
                        }
                    });
                    snackbar.show();
                }
            }
        }, SPLASH_DISPLAY_LENGTH);

        icon = (ImageView) findViewById(R.id.imageView3);
        text = (ImageView) findViewById(R.id.imageView4);
        setLocked(icon);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (AppStatus.getInstance(SplashActivity.this).isOnline()) {
                    setUnlocked(icon);
                }
            }
        }, SPLASH_IMAGE_LENGHT);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
