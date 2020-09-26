package com.ashomok.lullabies.ad;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ashomok.lullabies.R;
import com.ashomok.lullabies.utils.LogHelper;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AdMobNativeBannerAd extends AdMobAd {

    private static final String TAG = LogHelper.makeLogTag(AdMobNativeBannerAd.class);

    @Inject
    public AdMobNativeBannerAd(Context context, int adId) {
        super(context, adId);
    }

    @Override
    protected void init() {
        loadNativeAd()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ad -> {
                            if (parentLayout != null) {
                                populateUnifiedNativeAdView(parentLayout, ad);
                            }
                        }, throwable -> {
                            LogHelper.e(TAG, throwable.getMessage());
                        });
    }

    private void populateUnifiedNativeAdView(View adView, UnifiedNativeAd nativeAd) {

        int heightInPixels = AdSize.SMART_BANNER.getHeightInPixels(parentLayout.getContext());
        if (parentLayout instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) parentLayout.getLayoutParams();
            layoutParams.height = heightInPixels;
        } else {
            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) parentLayout.getLayoutParams();
            layoutParams.height = heightInPixels;
        }

        LogHelper.d(TAG, "on populateUnifiedNativeAdView");

        NativeAdViewHolder nativeAdViewHolder = new NativeAdViewHolder(adView);
        nativeAdViewHolder.adView.setVisibility(View.VISIBLE);

//         The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) nativeAdViewHolder.adView.getHeadlineView()).setText(nativeAd.getHeadline());

        if (nativeAd.getCallToAction() == null) {
            nativeAdViewHolder.adView.getCallToActionView().setVisibility(View.GONE);
        } else {
            nativeAdViewHolder.adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) nativeAdViewHolder.adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            nativeAdViewHolder.adView.getBodyView().setVisibility(View.GONE);
        } else {
            nativeAdViewHolder.adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) nativeAdViewHolder.adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getIcon() == null) {
            nativeAdViewHolder.adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) nativeAdViewHolder.adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            nativeAdViewHolder.adView.getIconView().setVisibility(View.VISIBLE);
        }

        nativeAdViewHolder.adView.setNativeAd(nativeAd);
    }

    private static class NativeAdViewHolder {
        UnifiedNativeAdView adView;

        NativeAdViewHolder(View view) {
            adView = view.findViewById(R.id.native_ad);
            adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
            adView.setBodyView(adView.findViewById(R.id.ad_body));
            adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
            adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        }
    }

    private Set<UnifiedNativeAd> loadNativeAdsAsync() {

        Set<UnifiedNativeAd> result = new HashSet<>();

        AdLoader.Builder builder = new AdLoader.Builder(context, context.getString(adid));

        builder.forUnifiedNativeAd(unifiedNativeAd -> {
            result.add(unifiedNativeAd);
            LogHelper.d(TAG, "native add loaded, total set size = " + result.size());
        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);
        builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {

                LogHelper.e(TAG, "onAdFailedToLoad,error code " + errorCode);
            }
        });
        AdLoader adLoader = builder.build();
        adLoader.loadAds(new AdRequest.Builder().build(), 5);
        return result;
    }

    private Single<UnifiedNativeAd> loadNativeAd() {

        return Single.create(emitter -> {

            AdLoader.Builder builder = new AdLoader.Builder(context, context.getString(adid));

            builder.forUnifiedNativeAd(ad -> {
                        emitter.onSuccess(ad);
                        LogHelper.d(TAG, "Native ad loaded: " + ad.toString());
                    }
            );

            VideoOptions videoOptions = new VideoOptions.Builder()
                    .setStartMuted(true)
                    .build();

            NativeAdOptions adOptions = new NativeAdOptions.Builder()
                    .setVideoOptions(videoOptions)
                    .build();

            builder.withNativeAdOptions(adOptions);

            builder.withAdListener(getAdListener());

            AdLoader adLoader = builder.build();
            adLoader.loadAd(new AdRequest.Builder().build());
        });
    }
}
