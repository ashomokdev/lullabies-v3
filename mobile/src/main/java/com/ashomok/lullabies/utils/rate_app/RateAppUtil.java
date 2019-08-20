package com.ashomok.lullabies.utils.rate_app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.ashomok.lullabies.R;


public class RateAppUtil {
    public void rate(Activity activity) {
        Toast.makeText(activity, R.string.thank_you_for_your_support, Toast.LENGTH_SHORT).show();
        String appPackageName = activity.getPackageName();
        openPackageInMarket("https://play.google.com/store/apps/details?id=" + appPackageName,
                appPackageName, activity);
    }

    public void openPackageInMarket(String uri, String appPackageName, Activity activity) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
    }
}
