package com.riccardobusetti.colombo.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;
import com.riccardobusetti.colombo.R;

/**
 * Created by riccardobusetti on 10/06/16.
 */

public class MainIntroActivity extends IntroActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        /**
         * Standard slide (like Google's intros)
         */
        addSlide(new SimpleSlide.Builder()
                .title(R.string.title_1)
                .description(R.string.description_1)
                .image(R.drawable.ic_app)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.title_2)
                .description(R.string.description_2)
                .image(R.drawable.ic_search_intro)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.title_3)
                .description(R.string.description_3)
                .image(R.drawable.ic_incognito_intro)
                .background(R.color.colorPrimaryIncognito)
                .backgroundDark(R.color.colorPrimaryDarkIncognito)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.title_storage)
                .description(R.string.description_storage)
                .image(R.drawable.ic_download_intro)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.title_gps)
                .description(R.string.description_gps)
                .image(R.drawable.ic_gps_intro)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .permission(Manifest.permission.ACCESS_FINE_LOCATION)
                .build());
    }
    
        public void onBackPressed() {
        super.onBackPressed();
        MainIntroActivity.this.finish(); Intent intent1 = new Intent(MainIntroActivity.this,MainActivity.class); intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }


}
