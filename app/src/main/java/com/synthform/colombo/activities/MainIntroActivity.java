package com.synthform.colombo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;
import com.synthform.colombo.R;

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

        addSlide(new SimpleSlide.Builder()
                .title(R.string.title_1)
                .description(R.string.description_1)
                .image(R.drawable.ic_slide_1)
                .background(R.color.colorSlide1)
                .backgroundDark(R.color.colorSlide1Dark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.title_2)
                .description(R.string.description_2)
                .image(R.drawable.ic_slide_2)
                .background(R.color.colorSlide2)
                .backgroundDark(R.color.colorSlide2Dark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.title_3)
                .description(R.string.description_3)
                .image(R.drawable.ic_slide_3)
                .background(R.color.colorSlide3)
                .backgroundDark(R.color.colorSlide3Dark)
                .build());
    }

    public void onBackPressed() {
        super.onBackPressed();
        MainIntroActivity.this.finish();
        Intent intent1 = new Intent(MainIntroActivity.this, MainActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
