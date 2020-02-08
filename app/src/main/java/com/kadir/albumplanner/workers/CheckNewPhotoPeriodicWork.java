package com.kadir.albumplanner.workers;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.kadir.albumplanner.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CheckNewPhotoPeriodicWork extends Worker {
    private static final String TAG = "Check New Photo";
    private static final String CHANNEL_ID = "channel1";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    private static final String IMAGE_DIRECTORY_NAME = "CameraApp";


    File photoFile = null;

    private String mCurrentPhotoPath;
    Context context;

    public CheckNewPhotoPeriodicWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }


    @NonNull
    @Override
    public Result doWork() {




        int sayi = Integer.MAX_VALUE;
        float b = 0;
        double d = 0;
        String kelime = "yusuf";
        char a = 'y';
        char u =  'u';
        char z = 's';
        char v =  'u';
        char x = 'f';




































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











//    private void captureImage()
//    {
//                // Create the File where the photo should go
//                try {
//                    photoFile = null;
//                    photoFile = createExternalStoragePublicPicture();
//
//                    // Continue only if the File was successfully created
//                    if (photoFile != null) {
//                        mCurrentPhotoPath = photoFile.getAbsolutePath();
//                        Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
//                                "com.kadir.albumplanner.fileprovider",
//                                photoFile);
//                        Log.v("ASD", "path : " + mCurrentPhotoPath);
//                        galleryAddPic();
//
//                    }
//                } catch (Exception ex) {
//                    // Error occurred while creating the File
//                    Log.v("ASD","captureImage/else/if/catch/error occured");
//                }
//        }
//
//
//
//
//
//    private File createExternalStoragePublicPicture() {
//        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//
//        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + name + "/");
//        File imageFile = null;
//
////        Date lastModDate = new Date(storageDir.lastModified());
////        Log.i("ASD","File last modified @ : "+ lastModDate.toString());
//
//        if(!storageDir.exists()){
//            storageDir.mkdir();
//
//            String aa = Environment.getExternalStorageDirectory().getAbsolutePath();
//            Log.v("ASD", "sdfsdgf" + aa);
//        }
//        try {
//            imageFile = File.createTempFile(name,".jpg", storageDir);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return imageFile;
//    }
//
//
//    private void galleryAddPic() {
//        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        File f = new File(mCurrentPhotoPath);
//        Uri contentUri = Uri.fromFile(f);
//        mediaScanIntent.setData(contentUri);
//        this.sendBroadcast(mediaScanIntent);
//    }
//

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
//        gallery.setData()
    }



}
