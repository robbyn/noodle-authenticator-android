package org.tastefuljava.noodleauth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutBox {
    private static final String TAG = AboutBox.class.getName();

    public static void show(Activity activity) {
        try {
            //Use a Spannable to allow for links highlighting
            String source = "Version " + versionName(activity) + "\n"
                    + activity.getString(R.string.about);
            LayoutInflater inflater = activity.getLayoutInflater();
            View about = inflater.inflate(R.layout.about_dialog,null);
            TextView tvAbout = about.findViewById(R.id.aboutText);
            tvAbout.setText(source);
            new AlertDialog.Builder(activity)
                    .setTitle("About " + activity.getString(R.string.app_name))
                    .setCancelable(true)
                    .setIcon(R.drawable.ic_launcher_foreground)
                    .setPositiveButton("OK", null)
                    .setView(about)
                    .show();    //Builder method returns allow for method chaining
        } catch(InflateException e) {
            Log.e(TAG, "Error inflating about box");
        }
    }

    static String versionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Unknown";
        }
    }
}
