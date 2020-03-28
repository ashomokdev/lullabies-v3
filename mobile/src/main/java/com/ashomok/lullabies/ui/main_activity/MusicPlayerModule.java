package com.ashomok.lullabies.ui.main_activity;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.ashomok.lullabies.BuildConfig;
import com.ashomok.lullabies.R;
import com.ashomok.lullabies.ad.AdMobContainer;
import com.ashomok.lullabies.ad.AdMobContainerImpl;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MusicPlayerModule {

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
            } else {
                return R.string.test_banner;
            }
        } else {
            if (mediaId.contains(context.getResources().getString(R.string.classic_key))) {
                if (BuildConfig.IS_NATIVE_AD_ACTIVE && getRandomBoolean()) {
                    return R.string.lullabies_main_activity_classic_tones_native;
                } else {
                    return R.string.lullabies_main_activity_classic_tones_banner;
                }
            } else {
                return R.string.lullabies_main_activity_base_collection_banner;
            }
        }
    }

    private static boolean getRandomBoolean() { return Math.random() < 0.5; }

    @Provides
    static AdMobContainer provideAdMobContainer(Context context, @StringRes int adMobId) {
        AdMobContainerImpl.AdType adType;
        if (adMobId == R.string.lullabies_main_activity_classic_tones_native
                || adMobId == R.string.native_ad_test_banner) {
            adType = AdMobContainerImpl.AdType.NATIVE;
        } else {
            adType = AdMobContainerImpl.AdType.BANNER;
        }
        return new AdMobContainerImpl(context, adMobId, adType);
    }

    @Binds
    abstract MusicPlayerContract.Presenter mainPresenter(MusicPlayerPresenter presenter);

    @ContributesAndroidInjector
    abstract MediaBrowserFragment mediaBrowserFragment(); //todo don't inject - simplify
}
