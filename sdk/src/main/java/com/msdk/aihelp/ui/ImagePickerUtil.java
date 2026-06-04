package com.msdk.aihelp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImagePickerUtil {

    public static final int REQUEST_IMAGE_PICK = 9001;
    public static final int REQUEST_IMAGE_CAPTURE = 9002;
    public static final int REQUEST_PERMISSION_CAMERA = 9003;

    public static void openGallery(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    public static File openCamera(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
            return null;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = createImageFile(activity);
        if (photoFile != null) {
            Uri photoUri = FileProvider.getUriForFile(activity,
                    activity.getPackageName() + ".aihelp.fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
        return photoFile;
    }

    private static File createImageFile(Activity activity) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "AIHELP_" + timeStamp;
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(fileName, ".jpg", storageDir);
        } catch (IOException e) {
            return null;
        }
    }
}
