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
    private static final String PRODUCT_ID_1 = "com.riccardobusetti.colombo.coke";
    private static final String PRODUCT_ID_2 = "com.riccardobusetti.colombo.brioches";
    private static final String PRODUCT_ID_3 = "com.riccardobusetti.colombo.kebab";
    private static final String PRODUCT_ID_4 = "com.riccardobusetti.colombo.kingmeal";
    private static final String PRODUCT_ID_5 = "com.riccardobusetti.colombo.present";
    private static final String PRODUCT_ID_6 = "com.riccardobusetti.colombo.computer";
    private static final String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqZtUFo4j56eXaLF9eUq0wBgAkmN5AcAn7rkt2laVFIj607r/5xhrb9fG+eEZqyuqB7eNuqn96mM64BUCR4uqBNuzfBribzPqPWLM1P7jxMaOY4/CKUtQX/Asaf/Vc+n94nT563s+eEe+Yg4sydHX+qh1bcSrbgRsDJqfSP3HRjfb6Xa2Bq3Xg9L+rzX4dBczYO0ik7asZ98WNRFH/OLb61unjTe37T7tmyi1R5uCriJoMgJmvZG+OaPTqWZkBaRSmPZIfc37mA/W0Ueih6bkZHwzSbFT1hW8Z7Ld8vVQgF41E3OFIrv/TMgR7R7tO1JgvCkLPmE+jVzPUMotP7S8FQIDAQAB";

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
