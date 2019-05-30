package com.ashomok.lullabies.ui.main_activity;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.StringRes;

import com.ashomok.lullabies.BuildConfig;
import com.ashomok.lullabies.R;
import com.ashomok.lullabies.ad.AdMobContainer;
import com.ashomok.lullabies.ad.AdMobContainerImpl;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class MusicPlayerModule {
    @Provides
    static Activity provideActivity(MusicPlayerActivity activity) {
        return activity;
    }

    @Provides
    static @StringRes
    int provideAdBannerId() {
        if (BuildConfig.DEBUG) {
            return R.string.test_banner;
        } else {
            return R.string.main_activity_banner;
        }
    }

    @Provides
    static AdMobContainer provideAdMobContainer(Context context, @StringRes int adMobId) {
       return new AdMobContainerImpl(context, adMobId);
    }

    @Binds
    abstract MusicPlayerContract.Presenter mainPresenter(MusicPlayerPresenter presenter);
}
