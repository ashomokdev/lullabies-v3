package com.ashomok.lullabies.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseAnalyticsHelper {

    private static FirebaseAnalyticsHelper instance;
    private final FirebaseAnalytics mFirebaseAnalytics;

    public static FirebaseAnalyticsHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseAnalyticsHelper(context);
        }
        return instance;
    }

    private FirebaseAnalyticsHelper(Context context){
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public void trackContentSelected(MediaBrowserCompat.MediaItem item){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, item.getMediaId());
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, item.getDescription().getTitle().toString());
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "MediaItem");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
}
