package com.msdk.aihelp.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageCompressor {

    private static final int MAX_DIMENSION = 1280;
    private static final int QUALITY = 80;

    public static File compress(File source, File outputDir) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(source.getAbsolutePath(), options);

        int width = options.outWidth;
        int height = options.outHeight;
        int sampleSize = calculateSampleSize(width, height);

        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        Bitmap bitmap = BitmapFactory.decodeFile(source.getAbsolutePath(), options);
        if (bitmap == null) {
            throw new IOException("Failed to decode image: " + source.getAbsolutePath());
        }

        Bitmap scaled = scaleBitmap(bitmap, MAX_DIMENSION);
        if (scaled != bitmap) {
            bitmap.recycle();
        }

        File output = new File(outputDir, "compressed_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(output)) {
            scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, fos);
        } finally {
            scaled.recycle();
        }
        return output;
    }

    static int calculateSampleSize(int width, int height) {
        int sampleSize = 1;
        while (width / sampleSize > MAX_DIMENSION * 2 || height / sampleSize > MAX_DIMENSION * 2) {
            sampleSize *= 2;
        }
        return sampleSize;
    }

    static Bitmap scaleBitmap(Bitmap bitmap, int maxDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= maxDimension && height <= maxDimension) {
            return bitmap;
        }
        float ratio = Math.min((float) maxDimension / width, (float) maxDimension / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}
