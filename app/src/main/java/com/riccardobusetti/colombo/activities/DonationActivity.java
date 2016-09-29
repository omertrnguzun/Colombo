package com.riccardobusetti.colombo.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.riccardobusetti.colombo.R;

public class DonationActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler{

    private BillingProcessor bp;
    private Toolbar toolbar;
    private static final String PRODUCT_ID_1 = "colombo.coke";
    private static final String PRODUCT_ID_2 = "colombo.brioches";
    private static final String PRODUCT_ID_3 = "colombo.kebab";
    private static final String PRODUCT_ID_4 = "colombo.kingmeal";
    private static final String PRODUCT_ID_5 = "colombo.present";
    private static final String PRODUCT_ID_6 = "colombo.computer";
    private static final String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAi1ml9iQBihL7CYuSU3xREwxN2AlYQciQgYnCK90X22siw5J2IT65iCr4YQwQL8Y1Kq/ApNcgUQ1wZluJU3KcUMTFkPF+h0Gn0cMAFCWPh7gm1T1P75Aul3B9azYFDGn+x3wfiTi4qYNF+eT/5xZM6vIaN5L4Hr0Mn3tWn6ZSx5ZJtBg/gMu1x/7PLtNrez39RmW9ngXlndpoJM4Cv+xDRUS+o8BX4szOBa2rk/n4LeOjplVJl8FvLYR83ShkGaEbMeb8bCp+D5PnC+zQ0pktFjuARpVpmMDfzOB3gj3+p7y6mb0FtIXhF3RPzyNJtBlAaUVA1CmobM8mJBqPiBbTrQIDAQAB";

    private RelativeLayout coke, brioches, kebab, kingMeal, present, computer;

    private SharedPreferences prefs;

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
        setContentView(R.layout.activity_donation);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        bp = new BillingProcessor(this, LICENSE_KEY, this);

        coke = (RelativeLayout) findViewById(R.id.layoutCoke);
        brioches = (RelativeLayout) findViewById(R.id.layoutBrioches);
        kebab = (RelativeLayout) findViewById(R.id.layoutKebab);
        kingMeal = (RelativeLayout) findViewById(R.id.layoutKing);
        present = (RelativeLayout) findViewById(R.id.layoutPresent);
        computer = (RelativeLayout) findViewById(R.id.layoutPC);

        setUpClicks();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void setUpClicks() {
        coke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(DonationActivity.this, PRODUCT_ID_1);
            }
        });

        brioches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(DonationActivity.this, PRODUCT_ID_2);
            }
        });

        kebab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(DonationActivity.this, PRODUCT_ID_3);
            }
        });

        kingMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(DonationActivity.this, PRODUCT_ID_4);
            }
        });

        present.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(DonationActivity.this, PRODUCT_ID_5);
            }
        });

        computer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bp.purchase(DonationActivity.this, PRODUCT_ID_6);
            }
        });
    }

    @Override
    public void onDestroy() {
        if (bp != null)
            bp.release();

        super.onDestroy();
    }
}
