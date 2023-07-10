package com.scanlibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jhansi on 05/04/15.
 */
public class Utils {

    private Utils() {

    }

    public static Uri getUri(Context context, Bitmap bitmap) {

        deleteFileOnExit();
        try {
            File folder = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .getAbsolutePath() + "/swiftScanTemp");
            if (!folder.exists()){
                folder.mkdir();
            }
            File file = new File(folder + "/temp.jpg");
            if (!file.exists())
                file.createNewFile();
            FileOutputStream mFileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, mFileOutputStream);
            mFileOutputStream.close();
            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Bitmap getBitmap(Context context, Uri uri) throws IOException {
        try {
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void deleteFileOnExit() {
        try {
            File folder = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .getAbsolutePath() + "/swiftScanTemp");
            if (folder.exists()) {
                deleteDir(folder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
}