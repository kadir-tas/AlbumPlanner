package com.kadir.albumplanner.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.kadir.albumplanner.R;


public class CheckNewPhotoPeriodicWork extends Worker {
    private static final String TAG = "Check New Photo";
    private static final String CHANNEL_ID = "channel1";


    public CheckNewPhotoPeriodicWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (getApplicationContext().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                WorkManager instance = WorkManager.getInstance();
                instance.cancelAllWorkByTag(TAG);
                sendNotification("Album Planner","Allow your f*ck n permission",0);
                Log.v(TAG, "Permission is revoked2");
                return Result.failure();
            }
        }
        //permission is automatically granted on sdk<23 upon installation or user allowed permission
        Log.d("AAA","Çalışıyorum");

        check();

        return Result.success();
    }

    public boolean check(){

        return false;
    }


    private void sendNotification(String title, String text, int id) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
        intent.setData(uri);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManager notificationManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    title,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(text);
            notificationManager.createNotificationChannel(channel);
        }
            notificationManager.notify(id, builder.build());

    }
}
