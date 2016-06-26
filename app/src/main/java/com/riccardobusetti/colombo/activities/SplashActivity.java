package com.riccardobusetti.colombo.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.riccardobusetti.colombo.R;

/**
 * Created by riccardobusetti on 26/06/16.
 */

public class SplashActivity extends Activity {

    private static final int SPLASH_DISPLAY_LENGTH = 1500;

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

        final View icon = findViewById(R.id.imageView3);
        final View text = findViewById(R.id.imageView4);

        text.animate()
                .alpha(0)
                .setDuration(0);

        icon.animate()
                .scaleX(0)
                .scaleY(0)
                .alpha(0f)
                .setDuration(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        icon.animate()
                                .scaleX(1)
                                .scaleY(1)
                                .alpha(1f)
                                .translationY(-100)
                                .setDuration(400)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        text.animate()
                                                .alpha(1f)
                                                .setDuration(200);
                                    }
                                });
                    }
                });
    }
}
