package com.ashomok.lullabies.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class StartServiceUtil {

    private static final String TAG = LogHelper.makeLogTag(StartServiceUtil.class);

    public static void startService (Context context, Intent intent){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }

        LogHelper.d(TAG, "startService called");
    }
}
