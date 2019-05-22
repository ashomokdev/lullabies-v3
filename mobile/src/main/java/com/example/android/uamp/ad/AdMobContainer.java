package com.example.android.uamp.ad;

import android.view.ViewGroup;

/**
 * Created by iuliia on 7/25/16.
 */
public interface AdMobContainer {
    void initBottomBannerAd(ViewGroup parentLayout);

    void showAd(boolean isAdsActive);
}
