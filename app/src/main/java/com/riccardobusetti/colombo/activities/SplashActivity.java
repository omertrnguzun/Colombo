package com.riccardobusetti.colombo.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.riccardobusetti.colombo.R;

/**
 * Created by riccardobusetti on 26/06/16.
 */

public class SplashActivity extends Activity {

    private static final int SPLASH_DISPLAY_LENGTH = 1000;
    private static final int SPLASH_IMAGE_LENGHT = 700;
    private ImageView icon, text;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, SPLASH_DISPLAY_LENGTH);

        icon = (ImageView) findViewById(R.id.imageView3);
        text = (ImageView) findViewById(R.id.imageView4);
        setLocked(icon);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setUnlocked(icon);
            }
        }, SPLASH_IMAGE_LENGHT);
    }

    public static void  setLocked(ImageView v) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);  //0 means grayscale
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        v.setColorFilter(cf);
    }

    public static void  setUnlocked(ImageView v) {
        v.setColorFilter(null);
    }
}
