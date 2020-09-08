package com.ashomok.lullabies.ui.main_activity;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.ashomok.lullabies.BuildConfig;
import com.ashomok.lullabies.R;
import com.ashomok.lullabies.ad.AdMobAd;
import com.ashomok.lullabies.ad.AdMobAdaptiveBannerAd;
import com.ashomok.lullabies.ad.AdMobBannerAd;
import com.ashomok.lullabies.ad.AdMobNativeBannerAd;
import com.ashomok.lullabies.utils.LogHelper;

import java.util.Random;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MusicPlayerModule {
    private static final String TAG = LogHelper.makeLogTag(MusicPlayerModule.class);

    @Binds
    abstract AppCompatActivity provideAppCompatActivity(MusicPlayerActivity activity);

    @Binds
    abstract Activity provideActivity(MusicPlayerActivity activity);

    @Provides
    static @NonNull
    String provideMediaId(MusicPlayerActivity activity) {
        return activity.getMediaId();
    }

    @Provides
    static @StringRes
    int provideAdMobId(Context context, String mediaId) {
        if (BuildConfig.DEBUG) {
            boolean randomBoolean = getRandomBoolean();
            if (BuildConfig.IS_NATIVE_AD_ACTIVE &&
                    (mediaId.contains(context.getResources().getString(R.string.classic_key)) || mediaId.contains(context.getResources().getString(R.string.mom_songs_key)))) {

                if (randomBoolean) {
                    return R.string.native_ad_test_banner;
                } else {
                    return R.string.test_banner;
                }
            } else {
                return R.string.test_banner;
            }
        } else {
            int random = getRandomInt();
            if (mediaId.contains(context.getResources().getString(R.string.classic_key))) {
                if (random == 0 && BuildConfig.IS_NATIVE_AD_ACTIVE) {
                    return R.string.lullabies_main_activity_classic_tones_native;
                } else if (random == 1) {
                    return R.string.lullabies_main_activity_classic_tones_adaptive_banner;
                } else {
                    return R.string.lullabies_main_activity_classic_tones_banner;
                }
            } else if (mediaId.contains(context.getResources().getString(R.string.mom_songs_key))) {
                if (random == 0 && BuildConfig.IS_NATIVE_AD_ACTIVE) {
                    return R.string.lullabies_main_activity_mom_songs_native;
                } else if (random == 1) {
                    return R.string.lullabies_main_activity_mom_songs_adaptive_banner;
                } else {
                    return R.string.lullabies_main_activity_mom_songs_banner;
                }
            } else {
                return R.string.lullabies_main_activity_base_collection_banner;
            }
        }
    }

    private static boolean getRandomBoolean() {
        return Math.random() < 0.5;
    }

    private static int getRandomInt() {
        return new Random().nextInt(3); //0, 1, 2
    }

    @Provides
    static AdMobAd provideAdMobAd(Activity activity, @StringRes int adMobId) {
        LogHelper.d(TAG, adMobId);
        if (adMobId == R.string.lullabies_main_activity_classic_tones_native
                || adMobId == R.string.lullabies_main_activity_mom_songs_native
                || adMobId == R.string.native_ad_test_banner) {
            return new AdMobNativeBannerAd(activity, adMobId);
        } else if (adMobId == R.string.lullabies_main_activity_classic_tones_adaptive_banner
                || adMobId == R.string.lullabies_main_activity_mom_songs_adaptive_banner) {
            return new AdMobAdaptiveBannerAd(activity, adMobId);
        } else {
            return new AdMobBannerAd(activity, adMobId);
        }
    }

    @Binds
    abstract MusicPlayerContract.Presenter mainPresenter(MusicPlayerPresenter presenter);

    @ContributesAndroidInjector
    abstract MediaBrowserFragment mediaBrowserFragment();
}
