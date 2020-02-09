package com.kadir.albumplanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.kadir.albumplanner.utils.Constants;
import com.kadir.albumplanner.workers.CheckNewPhotoPeriodicWork;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int STORAGE_PERMISSION_REQ_CODE = 1002;
    public static final int DEVICE_VERSION = Build.VERSION.SDK_INT;

    private Uri imageUri = null;
    private boolean saved = false;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    String imageLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getApplicationContext().getSharedPreferences("routine_cont_time_millis", MODE_PRIVATE);
        editor = pref.edit();
        setlastRoutineControlTime(Long.parseLong("1481275429089"));
        try {
            permissionGranted();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TimePicker tp = findViewById(R.id.timePicker1);


        //            String[] projection = new String[]{
//                    MediaStore.Images.ImageColumns._ID,
//                    MediaStore.Images.ImageColumns.DATA,
//                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
//                    MediaStore.Images.ImageColumns.DATE_TAKEN,
//                    MediaStore.Images.ImageColumns.MIME_TYPE
//            };
//            String path = "Camera";
//            String selection = MediaStore.Files.FileColumns.RELATIVE_PATH + " like ? ";
//            String selectionargs[] = new String[]{"%" + path + "%"};
//
//            cursor = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    projection, selection, selectionargs, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
//
//// Put it in the image view
//            if (cursor.moveToFirst()) {
//                String imageLocation = cursor.getString(1);
//                File imageFile = new File(imageLocation);
//                if (imageFile.exists()) {
//                    bm = BitmapFactory.decodeFile(imageLocation);
//                }
//            }


        //            String[] projection = new String[]{
//                    MediaStore.Images.ImageColumns.DATA,
//
//            };
//            String path = "Camera";
//            String selection = MediaStore.Files.FileColumns.RELATIVE_PATH + " like ? ";
//            String selectionargs[] = new String[]{"%" + path + "%"};
//
//            cursor = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    projection, selection, selectionargs, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
//            if (cursor.moveToFirst()) {
//                // You can replace '0' by 'cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)'
//                // Note that now, you read the column '_ID' and not the column 'DATA'
//                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)));
//                try {
//                    bm = BitmapFactory.decodeFile(imageLocation);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//
//
//
//            }


        //   tp.setIs24HourView(true);


//    }
    }

    public void permissionGranted() throws IOException {

        if (DEVICE_VERSION >= 29) {
            //NOTE: This method and its functions compatible with API Level 24 and above but I am using for only 29 and above
            List<UriPermission> permissions = MainActivity.this.getContentResolver().getPersistedUriPermissions();
            if (permissions.isEmpty()) {
                StorageManager manager = getSystemService(StorageManager.class);
                StorageVolume primaryStorageVolume = manager.getPrimaryStorageVolume();
                startActivityForResult(primaryStorageVolume.createOpenDocumentTreeIntent(), STORAGE_PERMISSION_REQ_CODE);
//                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//                intent.addFlags(
//                        Intent.FLAG_GRANT_READ_URI_PERMISSION
//                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//                                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
//                                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
//                startActivityForResult(intent, STORAGE_PERMISSION_REQ_CODE);
            } else {
                for (UriPermission up : permissions) {
                    String permissionUri = up.getUri().toString();
                    if (permissionUri.contains("DCIM") || permissionUri.endsWith("%3A") || permissionUri.endsWith("A")) {
                        requestExternalStoragePermission();
                        return;
                    }
                }
                StorageManager manager = getSystemService(StorageManager.class);
                StorageVolume primaryStorageVolume = manager.getPrimaryStorageVolume();
                startActivityForResult(primaryStorageVolume.createOpenDocumentTreeIntent(), STORAGE_PERMISSION_REQ_CODE);
//                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//                intent.addFlags(
//                        Intent.FLAG_GRANT_READ_URI_PERMISSION
//                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//                                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
//                                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
//                startActivityForResult(intent, STORAGE_PERMISSION_REQ_CODE);
            }
        } else if (DEVICE_VERSION > 23) {
            requestExternalStoragePermission();
        } else {
            startPeriodicRequest();
            scanImages();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestExternalStoragePermission() throws IOException {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission is granted2");
            startPeriodicRequest();
            scanImages();
        } else {
            Log.v(TAG, "Permission is revoked2");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == STORAGE_PERMISSION_REQ_CODE) {
            final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            MainActivity.this.getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
            startPeriodicRequest();
            try {
                requestExternalStoragePermission();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        } else {
            finish();
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
                    try {
                        scanImages();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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


    private boolean saveImage(Bitmap bitmap, @NonNull String name) throws IOException {
        OutputStream fos;
        Uri imageUri = null;
//        BufferedInputStream bis = BufferedInputStream(FileInputStream(file));
//        String contentType = URLConnection.guessContentTypeFromStream(bis);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = MainActivity.this.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name + ".jpg");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + "aaa");
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
        } else {
            String imagesSaveLocation = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES).toString() + File.separator + "aaa";
            File file = new File(imagesSaveLocation);
            if (!file.exists()) {
                file.mkdir();
            }
//            MoveFile(imageLocation,imagesSaveLocation);
            File image = new File(imagesSaveLocation, name + ".jpg");
            imageUri = Uri.fromFile(image);
            fos = new FileOutputStream(image);
        }
        saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();

        if (saved) {
            if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT <= 28) {
                Intent mediaScanIntent = new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(imageUri);
                MainActivity.this.sendBroadcast(mediaScanIntent);
            }
            return true;
        }
        return false;
    }

    public static void MoveFile(String path_source, String path_destination) throws IOException {
        File file_Source = new File(path_source);
        File file_Destination = new File(path_destination);

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(file_Source).getChannel();
            destination = new FileOutputStream(file_Destination).getChannel();

            long count = 0;
            long size = source.size();
            while ((count += destination.transferFrom(source, count, size - count)) < size) ;
            file_Source.delete();
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    private void deleteImage() throws FileNotFoundException {
        if (Build.VERSION.SDK_INT >= 29) {
            DocumentsContract.deleteDocument(MainActivity.this.getContentResolver(), imageUri);
        } else {
            ContentResolver contentResolver = getContentResolver();
            contentResolver.delete(imageUri, null, null);
        }
    }


//    private void scanImages() throws IOException {
//
//
//
//
//        Bitmap bm = null;
//        ImageView imageView = findViewById(R.id.pictureView);
//        Cursor cursor;
//        if (DEVICE_VERSION >= 29) {
//            String[] projection = new String[]{
//                    MediaStore.Images.ImageColumns._ID,
//                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
//                    MediaStore.Images.ImageColumns.DATE_TAKEN,
//                    MediaStore.Images.ImageColumns.RELATIVE_PATH,
//                    MediaStore.Images.ImageColumns.MIME_TYPE,
//                    MediaStore.Images.ImageColumns.DISPLAY_NAME
//            };
//            cursor = MainActivity.this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    projection,
//                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ? ",
//                    new String[]{"Camera"},
//                    MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
//            if (cursor.moveToFirst()) {
//                do {
//                    int dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN);
//                    long imageTakenTime = cursor.getLong(dateTakenColumn);
//                    if (compareDates(getLastRoutineControlTime(), imageTakenTime)) {
//                        imageUri = MediaStore.getDocumentUri(MainActivity.this, ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID))));
//                        ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(imageUri, "r");
//                        if (pfd != null) {
//                            bm = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
//                            if (saveImage(bm, "NAME" + System.currentTimeMillis())) {
//                                deleteImage();
//                            } else {
//                                return;
//                            }
//                        }
//                    } else {
//                        setlastRoutineControlTime(imageTakenTime);
//                        return;
//                    }
//                } while (cursor.moveToNext());
//            }
//        } else {
//            String[] projection = new String[]{
//                    MediaStore.Images.ImageColumns._ID,
//                    MediaStore.Images.ImageColumns.DATA,
//                    MediaStore.Images.ImageColumns.DATE_TAKEN
//            };
//            cursor = MainActivity.this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    projection,
//                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ? ",
//                    new String[]{"Camera"},
//                    MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
//            if (cursor.moveToFirst()) {
//
//                do {
//                    int dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN);
//                    long imageTakenTime = cursor.getLong(dateTakenColumn);
//                    if (compareDates(getLastRoutineControlTime(), imageTakenTime)) {
//                        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)));
//                        imageLocation = cursor.getString(1);
//                        File imageFile = new File(imageLocation);
//                        if (imageFile.exists()) {
//                            bm = BitmapFactory.decodeFile(imageLocation);
//                        }
//                        if (saveImage(bm, "NAME" + System.currentTimeMillis())) {
//                            deleteImage();
//                        } else {
//                            return;
//                        }
//                    } else {
//                        setlastRoutineControlTime(imageTakenTime);
//                    }
//                } while (cursor.moveToNext());
//            }
//        }
//        cursor.close();
//        imageView.setImageBitmap(bm);
//
//
//
//
//
//
//
//
//

//        BitmapFactory.Options options = null;
//        Bitmap bm = null;
//        ImageView imageView = findViewById(R.id.pictureView);
//        Cursor cursor;
//        Date dateTaken = null;
//        if (DEVICE_VERSION >= 29) {
//            String[] projection = new String[]{
//                    MediaStore.Images.ImageColumns._ID,
//                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
//                    MediaStore.Images.ImageColumns.DATE_TAKEN,
//                    MediaStore.Images.ImageColumns.RELATIVE_PATH,
//                    MediaStore.Images.ImageColumns.MIME_TYPE,
//                    MediaStore.Images.ImageColumns.DISPLAY_NAME
//            };
//            cursor = MainActivity.this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    projection,
//                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ? ",
//                    new String[]{"Camera"},
//                    MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
//            if (cursor.moveToFirst()) {
//                int dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN);
//                dateTaken = new Date(cursor.getLong(dateTakenColumn));
//                Log.d("DATE", dateTaken + "");
//                Log.d("DATE", cursor.getLong(dateTakenColumn) + "");
//                Log.d("DATE", cursor.getString(dateTakenColumn) + "");
//
////              DON'T DELETE THESE LINES. I'LL USE FOR IMPROVEMENT
////                for(int i = 0; i<cursor.getColumnCount(); i++){
////                    if(cursor.getString(i) == null){
////                        Log.d("NULL" + i, cursor.getColumnName(i) + " : NULL");
////                    }else{
////                        Log.d("ACK" + i, cursor.getColumnName(i) + " : " + cursor.getString(i));
////                    }
////                }
//
//                // We can replace '0' by 'cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)'
//                // Note that now, we read the column '_ID' and not the column 'DATA'
//                imageUri = MediaStore.getDocumentUri(MainActivity.this, ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID))));
////                imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)));
//                // now that we have the media URI, we can decode it to a bitmap
//                try (ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(imageUri, "r")) {
//                    if (pfd != null) {
//                        bm = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
//                    }
//                } catch (IOException ex) {
//
//                }
//            }
////-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
////-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//
///**
//
// String[] projection = new String[]{
// MediaStore.Images.ImageColumns._ID,
// MediaStore.Images.ImageColumns.RELATIVE_PATH,
// MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
// MediaStore.Images.ImageColumns.DATE_TAKEN,
// MediaStore.Images.ImageColumns.MIME_TYPE,
// MediaStore.Images.ImageColumns.DISPLAY_NAME,
// MediaStore.MediaColumns.TITLE,
// //                    THESE ARE ONLY FOR DEBUG
// //                    MediaStore.MediaColumns.BUCKET_ID,
// //                    MediaStore.MediaColumns.DATE_ADDED,
// //                    MediaStore.MediaColumns.DATE_EXPIRES,
// //                    MediaStore.MediaColumns.DATE_MODIFIED,
// //                    MediaStore.MediaColumns.DOCUMENT_ID,
// //                    MediaStore.MediaColumns.DURATION,
// //                    MediaStore.MediaColumns.HEIGHT,
// //                    MediaStore.MediaColumns.INSTANCE_ID,
// //                    MediaStore.MediaColumns.IS_PENDING,
// //                    MediaStore.MediaColumns.ORIENTATION,
// //                    MediaStore.MediaColumns.ORIGINAL_DOCUMENT_ID,
// //                    MediaStore.MediaColumns.OWNER_PACKAGE_NAME,
// //                    MediaStore.MediaColumns.SIZE,
// //                    MediaStore.MediaColumns.VOLUME_NAME,
// //                    MediaStore.MediaColumns.WIDTH,
// };
// String path = "Camera";
// String selection = MediaStore.Files.FileColumns.RELATIVE_PATH + " like ? ";
// String selectionargs[] = new String[]{"%/" + path + "/%"};
// Log.d("AAA", selection);
// for(String i : selectionargs) {
// Log.d("Aaa", i);
//
// }
// cursor = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
// projection, selection, selectionargs, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
//
// */
//
////-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
////-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//
//        } else {
//            String[] projection = new String[]{
//                    MediaStore.Images.ImageColumns._ID,
//                    MediaStore.Images.ImageColumns.DATA,
//                    MediaStore.Images.ImageColumns.DATE_TAKEN
//            };
//            cursor = MainActivity.this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    projection,
//                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ? ",
//                    new String[]{"Camera"},
//                    MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
//            if (cursor.moveToFirst()) {
//
////              DON'T DELETE THESE LINES. I'LL USE FOR IMPROVEMENT
////                for(int i = 0; i<cursor.getColumnCount(); i++){
////                    if(cursor.getString(i) == null){
////                        Log.d("NULL" + i, cursor.getColumnName(i) + " : NULL");
////                    }else{
////                        Log.d("ACK" + i, cursor.getColumnName(i) + " : " + cursor.getString(i));
////                    }
////                }
////                    Log.d("FF1", cursor.getString(0));
////                    Log.d("FF2", cursor.getString(1));
////                    Log.d("FF3", cursor.getString(2));
////                    Log.d("FF4", cursor.getString(3));
////                    Log.d("FF5", cursor.getString(4));
////                    Log.d("FF6", cursor.getString(5));
////                    Log.d("FF7", cursor.getString(6));
////                    Log.d("FF8", cursor.getString(7));
////                    int c = 0;
////                for(String i : cursor.getColumnNames()){
////                    Log.d("FF" + c++, i);
////                }
//
//                int dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN);
//                dateTaken = new Date(cursor.getLong(dateTakenColumn));
//                Log.d("DATE", dateTaken + "");
//                imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)));
//                imageLocation = cursor.getString(1);
//                File imageFile = new File(imageLocation);
//                if (imageFile.exists()) {
//                    bm = BitmapFactory.decodeFile(imageLocation);
//                }
//            }
//
//        }
//        cursor.close();
//        imageView.setImageBitmap(bm);
//        if (compareDates(getLastRoutineControlTime(), dateTaken)) {
//            try {
//                saveImage(bm, "NAME" + System.currentTimeMillis());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            if (saved) {
//                try {
//                    deleteImage();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    private boolean compareDates(Long lastCheckedPhotoDate, Long dateTaken) {
        return lastCheckedPhotoDate < dateTaken;
    }

    private Long getLastRoutineControlTime() {
        return pref.getLong("lastRoutineControlTime", 0);
    }

    private void setlastRoutineControlTime(Long lastRoutineControlTime) {
        editor.putLong("lastRoutineControlTime", lastRoutineControlTime);
        editor.apply();
    }

    /** This code works as scanImages2() function. Shorter than scanImages2(). But complexity of scanImages2() is better.

    public void scanImages() throws IOException {
        Bitmap bm = null;
        ImageView imageView = findViewById(R.id.pictureView);
        Cursor cursor;
        String[] projection;
        if (DEVICE_VERSION >= 29) {
            projection = new String[]{
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.DATE_TAKEN,
                    MediaStore.Images.ImageColumns.RELATIVE_PATH,
                    MediaStore.Images.ImageColumns.MIME_TYPE,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME
            };
        } else {
            projection = new String[]{
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.DATE_TAKEN
            };
        }

        cursor = MainActivity.this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ? ",
                new String[]{"Camera"},
                MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        if (cursor.moveToFirst()) {
            do {
                int dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN);
                long imageTakenTime = cursor.getLong(dateTakenColumn);
                if (compareDates(getLastRoutineControlTime(), imageTakenTime)) {
                    imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)));
                    if (DEVICE_VERSION > 19) {
                        ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(imageUri, "r");
                        if (pfd != null) {
                            bm = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                        }
                    } else {
                        imageLocation = cursor.getString(1);
                        File imageFile = new File(imageLocation);
                        if (imageFile.exists()) {
                            bm = BitmapFactory.decodeFile(imageLocation);
                        }
                    }
                    if (saveImage(bm, "NAME" + System.currentTimeMillis())) {
                        deleteImage();
                    } else {
                        return;
                    }
                } else {
                    setlastRoutineControlTime(imageTakenTime);
                    return;
                }
            } while (cursor.moveToNext());
            cursor.close();
            imageView.setImageBitmap(bm);
        }
    }

     */

    public void scanImages() throws IOException {
        Bitmap bm = null;
        ImageView imageView = findViewById(R.id.pictureView);
        Cursor cursor;
        if (DEVICE_VERSION >= 29) {
            String[] projection = new String[]{
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.DATE_TAKEN,
                    MediaStore.Images.ImageColumns.RELATIVE_PATH,
                    MediaStore.Images.ImageColumns.MIME_TYPE,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME
            };
            cursor = MainActivity.this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ? ",
                    new String[]{"Camera"},
                    MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
            if (cursor.moveToFirst()) {
                do {
                    int dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN);
                    long imageTakenTime = cursor.getLong(dateTakenColumn);
                    if (compareDates(getLastRoutineControlTime(), imageTakenTime)) {
                        imageUri = MediaStore.getDocumentUri(MainActivity.this, ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID))));
                        ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(imageUri, "r");
                            if (pfd != null) {
                                bm = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                                if (saveImage(bm, "NAME" + System.currentTimeMillis())) {
                                        deleteImage();
                                } else {
                                    return;
                                }
                            }
                    } else {
                        setlastRoutineControlTime(imageTakenTime);
                        return;
                    }
                } while (cursor.moveToNext());
            }
        } else {
            String[] projection = new String[]{
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.DATE_TAKEN
            };
            cursor = MainActivity.this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ? ",
                    new String[]{"Camera"},
                    MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
            if (cursor.moveToFirst()) {

                do {
                    int dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN);
                    long imageTakenTime = cursor.getLong(dateTakenColumn);
                    if (compareDates(getLastRoutineControlTime(), imageTakenTime)) {
                        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)));
                        imageLocation = cursor.getString(1);
                        File imageFile = new File(imageLocation);
                        if (imageFile.exists()) {
                            bm = BitmapFactory.decodeFile(imageLocation);
                        }
                            if (saveImage(bm, "NAME" + System.currentTimeMillis())) {
                                    deleteImage();
                            } else {
                                return;
                            }
                    } else {
                        setlastRoutineControlTime(imageTakenTime);
                    }
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        imageView.setImageBitmap(bm);
    }
}



