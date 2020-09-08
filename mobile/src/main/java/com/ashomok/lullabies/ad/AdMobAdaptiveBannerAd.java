package com.ashomok.lullabies.ad;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ashomok.lullabies.R;
import com.ashomok.lullabies.utils.LogHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;


public class AdMobAdaptiveBannerAd extends AdMobAd {

    private static final String TAG = LogHelper.makeLogTag(AdMobAdaptiveBannerAd.class);

    public AdMobAdaptiveBannerAd(Activity activity, int adId) {
        super(activity, adId);
    }

    @Override
    protected void init() {
        if (context.getResources().getConfiguration().orientation ==
                android.content.res.Configuration.ORIENTATION_PORTRAIT) {
            //init banner
            addBottomBanner(parentLayout);
        }
    }

    /**
     * add bottom banner on the parent view. Note: It may overlay some views.
     *
     * @param parent
     */
    private void addBottomBanner(ViewGroup parent) {
        if (parent instanceof RelativeLayout || parent instanceof LinearLayout) {
            AdView adView = new AdView(context);
            if (parent instanceof RelativeLayout) {

                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                adView.setLayoutParams(lp);

            } else {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 0.0f);
                adView.setLayoutParams(lp);
            }
            AdSize adSize = getAdSize();
            adView.setAdSize(adSize);
            adView.setAdUnitId(context.getResources().getString(adid));
            adView.setId(R.id.ad_banner);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.setAdListener(getAdListener());
            adView.loadAd(adRequest);
            parent.addView(adView);

            int heightInPixels = adSize.getHeightInPixels(parent.getContext());
            if (parent instanceof RelativeLayout) {
                RelativeLayout.LayoutParams layoutParams =
                        (RelativeLayout.LayoutParams) parent.getLayoutParams();
                layoutParams.height = heightInPixels;
            } else {
                LinearLayout.LayoutParams layoutParams =
                        (LinearLayout.LayoutParams) parent.getLayoutParams();
                layoutParams.height = heightInPixels;
            }
        } else {
            Log.e(TAG, "Ads can not been loaded programmaticaly. " +
                    "RelativeLayout and LinearLayout are supported as parent.");
        }
    }

    private AdSize getAdSize() {
        Activity activity = (Activity) context;
        Display display = activity.getWindowManager().getDefaultDisplay();

        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }
}
