package com.riccardobusetti.colombo.activities;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.amqtech.permissions.helper.objects.Permission;
import com.amqtech.permissions.helper.objects.Permissions;
import com.amqtech.permissions.helper.objects.PermissionsActivity;
import com.riccardobusetti.colombo.R;

/**
 * Created by riccardobusetti on 24/06/16.
 */

public class PermsActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        launchPerms();
    }

    /** Perms activity for set them if not in the first time */
    private void launchPerms() {
        new PermissionsActivity(getBaseContext())
                .withAppName(getResources().getString(R.string.app_name))
                .withPermissions(new Permission(Permissions.WRITE_EXTERNAL_STORAGE, "To download files, Colombo must have access to your storage!"), new Permission(Permissions.ACCESS_FINE_LOCATION, "If you want to use WebApps with geolocation Colombo must have access to your position!"))
                .withPermissionFlowCallback(new PermissionsActivity.PermissionFlowCallback() {
                    @Override
                    public void onPermissionGranted(Permission permission) {
                        Toast.makeText(PermsActivity.this, "The permissions are set!", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onPermissionDenied(Permission permission) {
                        Toast.makeText(PermsActivity.this, "You won't be able to download files!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setBackgroundColor(Color.parseColor("#80DEEA"))
                .setBarColor(Color.parseColor("#26C6DA"))
                .setStatusBarColor(Color.parseColor("#26C6DA"))
                .setMainTextColor(Color.parseColor("#262626"))
                .setMainTextColorSecondary(Color.parseColor("#262626"))
                .setIconColor(Color.parseColor("#262626"))
                .setStatusBarLight(true)
                .launch();
    }
}
