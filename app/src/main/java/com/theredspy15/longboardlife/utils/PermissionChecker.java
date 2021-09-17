package com.theredspy15.longboardlife.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class PermissionChecker { // TODO: handle denied permissions

    private static final int PERMISSION_CODE = 1015;

    public static boolean checkPermissionLocation(Context context) {
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkPermissionCamera(Context context) {
        return context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkPermissionGallery(Context context) {
        return context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermissionLocation(Context context, Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_CODE);
    }

    public static void requestPermissionCamera(Context context, Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.CAMERA }, PERMISSION_CODE);
    }
    public static void requestPermissionGallery(Context context, Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, PERMISSION_CODE);
    }

}
