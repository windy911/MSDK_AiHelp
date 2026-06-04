package com.msdk.aihelp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImagePickerUtil {

    public static final int REQUEST_IMAGE_PICK = 9001;
    public static final int REQUEST_IMAGE_CAPTURE = 9002;
    public static final int REQUEST_PERMISSION_CAMERA = 9003;

    public static void openGallery(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        fragment.startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    public static File openCamera(Fragment fragment) {
        Activity activity = fragment.getActivity();
        if (activity == null) return null;

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            fragment.requestPermissions(
                    new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
            return null;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = createImageFile(activity);
        if (photoFile != null) {
            Uri photoUri = FileProvider.getUriForFile(activity,
                    activity.getPackageName() + ".aihelp.fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            fragment.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
        return photoFile;
    }

    public static File uriToFile(Context context, Uri uri) {
        try {
            File cacheDir = context.getCacheDir();
            String fileName = "pick_" + System.currentTimeMillis() + ".jpg";
            File outFile = new File(cacheDir, fileName);
            InputStream in = context.getContentResolver().openInputStream(uri);
            if (in == null) return null;
            FileOutputStream out = new FileOutputStream(outFile);
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            return outFile;
        } catch (IOException e) {
            return null;
        }
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
