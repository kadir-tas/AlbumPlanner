package com.kadir.albumplanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TimePicker;

import com.kadir.albumplanner.utils.Constants;
import com.kadir.albumplanner.workers.CheckNewPhotoPeriodicWork;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TimePicker tp = findViewById(R.id.timePicker1);
        tp.setIs24HourView(true);
        writeStoragePermissionGranted();


    }

    public void writeStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted2");
                startPeriodicRequest();
            } else {

                Log.v(TAG, "Permission is revoked2");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
                    startPeriodicRequest();
                    return;
                } else {
                    finish();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startPeriodicRequest() {
        PeriodicWorkRequest.Builder checkNewPhotoBuilder = new PeriodicWorkRequest.Builder(
                CheckNewPhotoPeriodicWork.class, Constants.REPEAT_TIME_INTERVAL_IN_MINUTE, Constants.REPEAT_TIME_INTERVAL_UNITS)
                .setConstraints(new Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .setRequiresDeviceIdle(false)
                        .build());
        PeriodicWorkRequest periodicWorkRequest = checkNewPhotoBuilder.build();
        WorkManager.getInstance().enqueueUniquePeriodicWork("Check New Photo", ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest);
    }

}
