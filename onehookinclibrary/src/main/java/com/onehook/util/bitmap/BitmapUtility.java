package com.onehook.util.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by EagleDiao on 2016-05-30.
 */

public class BitmapUtility {

    public static String DEBUG_TAG = null;

    public static void rotatePhotoFile(final File file) {
        final Bitmap rotated = getRotatedBitmap(file);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            rotated.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void rotatePhotoFile(final File file, int rotation) {
        final long startTime = System.currentTimeMillis();
        if (rotation == 0) {
            /* do not do rotation if nothing to be rotated */
            return;
        }
        final Bitmap rotated = getRotatedBitmap(file, rotation);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            rotated.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (DEBUG_TAG != null) {
                    Log.d(DEBUG_TAG, "rotate photo file takes " + (System.currentTimeMillis() - startTime) + " ms");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getBitmapFileRotation(final File file) {
        final long startTime = System.currentTimeMillis();
        int rotate = 0;
        try {
            File imageFile = file;

            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
            if (DEBUG_TAG != null) {
                Log.d(DEBUG_TAG, "Exif orientation: " + orientation + " rotate " + rotate);
                Log.d(DEBUG_TAG, "Time spent to get exif info " + (System.currentTimeMillis() - startTime) + " ms");
            }
            return rotate;
        } catch (Exception e) {
            e.printStackTrace();
            if (DEBUG_TAG != null) {
                Log.d(DEBUG_TAG, "Failed to retrieve Exif orientation");
            }
        } finally {
            return rotate;
        }
    }

    public static Bitmap getRotatedBitmap(final File file, final int rotation) {
        Bitmap originalBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        final Bitmap rv = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
        if (rv != originalBitmap) {
            originalBitmap.recycle();
        }
        return rv;
    }

    public static Bitmap getRotatedBitmap(final File file) {
        int rotation = getBitmapFileRotation(file);
        return getRotatedBitmap(file, rotation);
    }

    public static Bitmap loadBitmapWithBestSize(final File file, final int longestLength) {
        try {
            int inWidth = 0;
            int inHeight = 0;

            FileInputStream in = new FileInputStream(file);

            // decode image size (decode metadata only, not the whole image)
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();

            // save width and height
            inWidth = options.outWidth;
            inHeight = options.outHeight;

            // decode full image pre-resized
            in = new FileInputStream(file);
            options = new BitmapFactory.Options();
            // calc rought re-size (this is no exact resize)
            options.inSampleSize = Math.max(inWidth / longestLength, inHeight / longestLength);

            // decode full image
            Bitmap roughBitmap = BitmapFactory.decodeStream(in, null, options);
            return roughBitmap;
        } catch (IOException e) {
            Log.e("Image", e.getMessage(), e);
            return null;
        }
    }
}
