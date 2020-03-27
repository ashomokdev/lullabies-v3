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
            return R.string.test_banner;
        } else {
            if (mediaId.contains(context.getResources().getString(R.string.classic_key))) {
                return R.string.lullabies_main_activity_classic_tones_banner;
            } else {
                return R.string.lullabies_main_activity_base_collection_banner;
            }
        }
    }

    @Provides
    static AdMobContainer provideAdMobContainer(Context context, @StringRes int adMobId) {
        return new AdMobContainerImpl(context, adMobId);
    }

    @Binds
    abstract MusicPlayerContract.Presenter mainPresenter(MusicPlayerPresenter presenter);

    @ContributesAndroidInjector
    abstract MediaBrowserFragment mediaBrowserFragment(); //todo don't inject - simplify
}
