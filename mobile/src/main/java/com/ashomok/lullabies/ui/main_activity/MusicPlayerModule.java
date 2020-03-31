package com.ashomok.lullabies.ui.main_activity;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.ashomok.lullabies.BuildConfig;
import com.ashomok.lullabies.R;
import com.ashomok.lullabies.ad.AdMobAd;
import com.ashomok.lullabies.ad.AdMobBannerAd;
import com.ashomok.lullabies.ad.AdMobNativeBannerAd;
import com.ashomok.lullabies.utils.LogHelper;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MusicPlayerModule {
    private static final String TAG = LogHelper.makeLogTag(MusicPlayerModule.class);

    @Provides
    static AppCompatActivity provideAppCompatActivity(MusicPlayerActivity activity) {
        return activity;
    }

    @Provides
    static @NonNull
    String provideMediaId(MusicPlayerActivity activity) {
        return activity.getMediaId();
    }

    @Provides
    static @StringRes
    int provideAdMobId(Context context, String mediaId) {
        if (BuildConfig.DEBUG) {
            if (BuildConfig.IS_NATIVE_AD_ACTIVE
                    && getRandomBoolean()
                    && mediaId.contains(context.getResources().getString(R.string.classic_key))) {
                return R.string.native_ad_test_banner;
            }
            else if (BuildConfig.IS_NATIVE_AD_ACTIVE
                    && getRandomBoolean()
                    && mediaId.contains(context.getResources().getString(R.string.mom_songs_key))) {
                return R.string.native_ad_test_banner;
            }
            else {
                return R.string.test_banner;
            }
        } else {
            if (mediaId.contains(context.getResources().getString(R.string.classic_key))) {
                if (BuildConfig.IS_NATIVE_AD_ACTIVE && getRandomBoolean()) {
                    return R.string.lullabies_main_activity_classic_tones_native;
                } else {
                    return R.string.lullabies_main_activity_classic_tones_banner;
                }
            }
            else if (mediaId.contains(context.getResources().getString(R.string.mom_songs_key))){
                if (BuildConfig.IS_NATIVE_AD_ACTIVE && getRandomBoolean()) {
                    return R.string.lullabies_main_activity_mom_songs_native;
                } else {
                    return R.string.lullabies_main_activity_mom_songs_banner;
                }
            }
            else {
                return R.string.lullabies_main_activity_base_collection_banner;
            }
        }
    }

    private static boolean getRandomBoolean() {
        return Math.random() < 0.5;
    }

    @Provides
    static AdMobAd provideAdMobAd(Context context, @StringRes int adMobId) {
        LogHelper.d(TAG, adMobId);
        if (adMobId == R.string.lullabies_main_activity_classic_tones_native
                || adMobId == R.string.native_ad_test_banner) {
            return new AdMobNativeBannerAd(context, adMobId);
        } else {
            return new AdMobBannerAd(context, adMobId);
        }
    }

    @Binds
    abstract MusicPlayerContract.Presenter mainPresenter(MusicPlayerPresenter presenter);

    @ContributesAndroidInjector
    abstract MediaBrowserFragment mediaBrowserFragment();
}
