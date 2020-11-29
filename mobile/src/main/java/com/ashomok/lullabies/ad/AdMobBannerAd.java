package com.ashomok.lullabies.ad;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ashomok.lullabies.R;
import com.ashomok.lullabies.utils.LogHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;


public class AdMobBannerAd extends AdMobAd {

    private static final String TAG = LogHelper.makeLogTag(AdMobBannerAd.class);

    public AdMobBannerAd(Context context, int adId) {
        super(context, adId);
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
            adView.setAdSize(AdSize.SMART_BANNER);
            adView.setAdUnitId(context.getResources().getString(adid));
            adView.setId(R.id.ad_banner);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.setAdListener(getAdListener());
            adView.loadAd(adRequest);
            parent.addView(adView);

            int heightInPixels = AdSize.SMART_BANNER.getHeightInPixels(parent.getContext());
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
            LogHelper.e(TAG, "Ads can not been loaded programmaticaly. " +
                    "RelativeLayout and LinearLayout are supported as parent.");
        }
    }
}
