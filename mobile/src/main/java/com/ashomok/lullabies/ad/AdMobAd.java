package com.ashomok.lullabies.ad;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.StringRes;

import com.ashomok.lullabies.R;
import com.ashomok.lullabies.utils.LogHelper;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.MobileAds;

/**
 * Created by iuliia on 7/25/16.
 */

public abstract class AdMobAd {

    protected final Context context;
    final int adid;
    ViewGroup parentLayout;
    private static final String TAG = LogHelper.makeLogTag(AdMobAd.class);
    private boolean adFailedToLoad = false;

    AdListener getAdListener() {
        return adListener;
    }

    private AdListener adListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            // Code to be executed when an ad finishes loading.
            adFailedToLoad = false;
            Log.d(TAG, "onAdLoaded");
        }

        @Override
        public void onAdFailedToLoad(int errorCode) {
            // Code to be executed when an ad request fails.
            adFailedToLoad = true;
            parentLayout.setVisibility(View.GONE); //hide ad layout room
            Log.d(TAG, "onAdFailedToLoad with code " + errorCode);
        }
    };

    public AdMobAd(Context context, @StringRes int adId) {
        this.context = context;
        this.adid = adId;
        String appId = context.getResources().getString(R.string.appID);
        MobileAds.initialize(context, appId);
    }

    public void initAd(ViewGroup parentLayout) {
        this.parentLayout = parentLayout;
        init();
    }

    protected abstract void init();

    public void showAd(boolean isAdsActive) {
        Log.w(TAG, "showAd with  " + isAdsActive + ", failed to load " + adFailedToLoad);
        if (!adFailedToLoad) {
            parentLayout.setVisibility(isAdsActive ? View.VISIBLE : View.GONE);
        }
    }
}
