package com.example.translate_objecttext;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

//Summary - Util class for requesting access to the Camera and Gallery

public class PermissionUtils {
    private static final String[] permissionsArray = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};

    public static final String SHARED_PREF = "Trial";


    public static boolean requestPermission(Activity activity, int requestCode) {

        boolean granted = true;
        ArrayList<String> permissionsNeeded = new ArrayList<>();

        for (String permission : permissionsArray) {
            int permissionCheck = ContextCompat.checkSelfPermission(activity, permission);
            boolean hasPermission = (permissionCheck == PackageManager.PERMISSION_GRANTED);
            granted &= hasPermission;
            if (!hasPermission) {
                permissionsNeeded.add(permission);
            }
        }

        if (granted) {
            return true;
        } else {
            ActivityCompat.requestPermissions(activity,
                    permissionsNeeded.toArray(new String[0]),
                    requestCode);
            return false;
        }
    }

    public static boolean permissionGranted(
            int requestCode, int permissionCode, int[] grantResults) {
        return requestCode == permissionCode && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean neverAskAgainSelected(final Activity activity) {
        for (String permission : permissionsArray) {
            final boolean prevShouldShowStatus = getRationaleDisplayStatus(activity, permission);
            final boolean currShouldShowStatus = activity.shouldShowRequestPermissionRationale(permission);
            return prevShouldShowStatus != currShouldShowStatus;
        }
        return false;
    }

    public static void setShouldShowStatus(final Context context, final String permission) {
        SharedPreferences genPrefs = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = genPrefs.edit();
        editor.putBoolean(permission, true);
        editor.commit();
    }

    public static boolean getRationaleDisplayStatus(final Context context, final String permission) {
        SharedPreferences genPrefs = context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        return genPrefs.getBoolean(permission, false);
    }

    public static void displayNeverAskAgainDialog(Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("We need to performing necessary task. Please permit the permission through "
                + "Settings screen.\n\nSelect Permissions -> Enable permission");
        builder.setCancelable(false);
        builder.setPositiveButton("Permit Manually", (dialog, which) -> {
            dialog.dismiss();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            context.startActivity(intent);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private static void gpsStatus(boolean isGPSEnable) {
        boolean isGps = isGPSEnable;
    }
}