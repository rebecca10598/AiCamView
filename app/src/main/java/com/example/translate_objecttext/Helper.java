package com.example.translate_objecttext;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Pair;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

//Summary - Checks the internet connection and provides custom toast of error messages

public class Helper {

    public static ProgressDialog showProgressDialog(Context context, String message) {
        ProgressDialog progressDialog = new ProgressDialog(context, R.style.progressDialog);
        progressDialog.setCancelable(false);
        if (!message.trim().isEmpty())
            progressDialog.setMessage(message);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progressDialog.show();
        return progressDialog;
    }

    public static boolean checkConnection(String errorString) {
        boolean error = false;
        if (errorString.contains("unable to resolve host") || errorString.contains("failed to connect") || errorString.contains("network is unreachable")
                || errorString.contains("software caused connection abort") || errorString.contains("connection timed out") || errorString.contains("No address associated with hostname")) {
            error = true;
        }
        return error;
    }

    public static Pair<String, String> GetErrorMessage(String errorString) {
        Pair<String, String> pair = new Pair<>("Something went Wrong","Can't find anything for you");

        if (errorString.contains("Unable to resolve host")) {

            pair = new Pair<>("Unable to Connect!", "Check your Internet Connection,Unable to connect the Server");

        } else if (errorString.contains("Failed to connect")) {

            pair = new Pair<>("Connection timed out", "Check your Internet Connection");

        } else if (errorString.contains("Network is unreachable")) {

            pair = new Pair<>("Network unreachable", "Could not connect to Internet, Check your mobile/wifi Connection");

        } else if (errorString.contains("Software caused connection abort")) {

            pair = new Pair<>("Connection Aborted", "Connection was aborted by server, without any response");

        } else if (errorString.contains("Connection timed out")) {

            pair = new Pair<>("Connection timed out", "Could not connect server, check internet connection");

        } else if (errorString.contains("No address associated with hostname")) {

            pair = new Pair<>("Unable to Connect!", "Check your Internet Connection,Unable to connect the Server");

        }
        return pair;
    }

    public static void ShowAlertDialog(final Context context, String title, String message, final boolean isFinish) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (isFinish)
                            ((Activity) context).finish();
                    }
                })
                .show();
    }

    public static void deleteFileOnExit() {
        try {
            File folder = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .getAbsolutePath() + "/swiftScanTemp");
            if (folder.exists()) {
                deleteDir(folder)
                ;
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
