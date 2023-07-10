package com.example.translate_objecttext;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;



import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class PermissionTest {

    @Test
    public void testCameraPermission() {
        Context context = ApplicationProvider.getApplicationContext();

        int cameraPermissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        boolean hasCameraPermission = cameraPermissionStatus == PackageManager.PERMISSION_GRANTED;

        Assert.assertTrue("Camera permission not granted", hasCameraPermission);
    }

    @Test
    public void testStoragePermission() {
        Context context = ApplicationProvider.getApplicationContext();

        int storagePermissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        boolean hasStoragePermission = storagePermissionStatus == PackageManager.PERMISSION_GRANTED;

        Assert.assertTrue("Storage permission not granted", hasStoragePermission);
    }
}
