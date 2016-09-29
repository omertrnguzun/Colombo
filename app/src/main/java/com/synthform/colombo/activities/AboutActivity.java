package com.synthform.colombo.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.synthform.colombo.R;
import com.synthform.colombo.view.ViewAnimationUtils;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AboutActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    private CoordinatorLayout coordinatorLayout;
    private TextView rate, github, donate;
    private RelativeLayout coke, brioches, kebab, kingMeal, present, computer, donateContainer;
    private Toolbar toolbar;
    private SharedPreferences prefs;
    private BillingProcessor bp;
    private static final String PRODUCT_ID_1 = "colombo.coke";
    private static final String PRODUCT_ID_2 = "colombo.brioches";
    private static final String PRODUCT_ID_3 = "colombo.kebab";
    private static final String PRODUCT_ID_4 = "colombo.kingmeal";
    private static final String PRODUCT_ID_5 = "colombo.present";
    private static final String PRODUCT_ID_6 = "colombo.computer";
    private static final String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAi1ml9iQBihL7CYuSU3xREwxN2AlYQciQgYnCK90X22siw5J2IT65iCr4YQwQL8Y1Kq/ApNcgUQ1wZluJU3KcUMTFkPF+h0Gn0cMAFCWPh7gm1T1P75Aul3B9azYFDGn+x3wfiTi4qYNF+eT/5xZM6vIaN5L4Hr0Mn3tWn6ZSx5ZJtBg/gMu1x/7PLtNrez39RmW9ngXlndpoJM4Cv+xDRUS+o8BX4szOBa2rk/n4LeOjplVJl8FvLYR83ShkGaEbMeb8bCp+D5PnC+zQ0pktFjuARpVpmMDfzOB3gj3+p7y6mb0FtIXhF3RPzyNJtBlAaUVA1CmobM8mJBqPiBbTrQIDAQAB";
    private boolean donateState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeDark);
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("hw_acceleration", true)) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }
        setContentView(R.layout.activity_about);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle("About");
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        bp = new BillingProcessor(this, LICENSE_KEY, this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordi_about);
        donateContainer = (RelativeLayout) findViewById(R.id.donateContainer);
        donateContainer.setVisibility(View.GONE);
        rate = (TextView) findViewById(R.id.rate);
        github = (TextView) findViewById(R.id.github);
        donate = (TextView) findViewById(R.id.donate);
        coke = (RelativeLayout) findViewById(R.id.layoutCoke);
        brioches = (RelativeLayout) findViewById(R.id.layoutBrioches);
        kebab = (RelativeLayout) findViewById(R.id.layoutKebab);
        kingMeal = (RelativeLayout) findViewById(R.id.layoutKing);
        present = (RelativeLayout) findViewById(R.id.layoutPresent);
        computer = (RelativeLayout) findViewById(R.id.layoutPC);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            coordinatorLayout.setSystemUiVisibility(coordinatorLayout.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        setUpClick();

    }

    private void setUpClick() {

        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.riccardobusetti.colombo");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://github.com/RiccardoBusetti/Colombo");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        donate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!donateState) { // Open donate
                    ViewAnimationUtils.expand(donateContainer);
                    donateState = true;
                } else { // Close donate
                    ViewAnimationUtils.collapse(donateContainer);
                    donateState = false;
                }
            }
        });

        coke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(AboutActivity.this, PRODUCT_ID_1);
            }
        });

        brioches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(AboutActivity.this, PRODUCT_ID_2);
            }
        });

        kebab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(AboutActivity.this, PRODUCT_ID_3);
            }
        });

        kingMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(AboutActivity.this, PRODUCT_ID_4);
            }
        });

        present.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(AboutActivity.this, PRODUCT_ID_5);
            }
        });

        computer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(AboutActivity.this, PRODUCT_ID_6);
            }
        });

    }

    @Override
    public void onDestroy() {
        if (bp != null)
            bp.release();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {

    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

    }

    @Override
    public void onBillingInitialized() {

    }
}
