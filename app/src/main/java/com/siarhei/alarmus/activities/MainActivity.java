package com.siarhei.alarmus.activities;

import android.Manifest;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.siarhei.alarmus.R;

public class MainActivity extends TabActivity {

    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 3;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabHost = getTabHost();
        //tabHost.setup();
        // инициализация была выполнена в getTabHost
        // метод setup вызывать не нужно

        TabHost.TabSpec tabSpec;
        //ImageView image= new ImageView(this);
        //image.setImageResource(R.drawable.ic_alarm1);
        //image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        //Drawable icon = getResources().getDrawable(R.drawable.ic_alarm1);
        tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setIndicator(getResources().getString(R.string.alarms));
        tabSpec.setContent(new Intent(this, AlarmListActivity.class));
        //tabSpec.setIndicator(image);
        //tabSpec.setIndicator("",icon);

        tabHost.addTab(tabSpec);
        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setIndicator(getResources().getString(R.string.map));
        tabSpec.setContent(new Intent(this, MapActivity.class));
        tabHost.addTab(tabSpec);
        if (!requestOverlaysPermission()) requestLocationPermission();
    }

    public boolean requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return true;
        }
        return false;
    }

    private boolean requestOverlaysPermission() {
        // Check if Android M or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            createPermissionsDialog().show();
            //startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            requestLocationPermission();
            Log.d("onActivityResult", "ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE");
        }
    }

    public AlertDialog createPermissionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.message_permissions);
        builder.setPositiveButton(R.string.ok, (dialog, id) -> {
          dialog.cancel();
        });
        builder.setOnCancelListener(dialog -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                   Uri.parse("package:" + this.getPackageName()));
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
        });
        AlertDialog dialog = builder.create();
        return dialog;
    }
}