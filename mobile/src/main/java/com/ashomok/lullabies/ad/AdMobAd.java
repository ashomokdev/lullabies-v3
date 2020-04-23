package com.ashomok.lullabies.ad;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.StringRes;

import com.ashomok.lullabies.R;
import com.google.android.gms.ads.MobileAds;

/**
 * Created by iuliia on 7/25/16.
 */
public abstract class AdMobAd {

    protected final Context context;
    protected final int adid;
    protected ViewGroup parentLayout;

    public AdMobAd(Context context, @StringRes int adId) {
        this.context = context;
        this.adid = adId;
        String appId = context.getResources().getString(R.string.appID);
        MobileAds.initialize(context, appId);
    }

    public void initAd(ViewGroup parentLayout){
        this.parentLayout = parentLayout;
        init();
    }

    protected abstract void init();

    public void showAd(boolean isAdsActive){
        parentLayout.setVisibility(isAdsActive ? View.VISIBLE : View.GONE);
     }
}
