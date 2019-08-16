package com.ashomok.lullabies.ui.full_screen_player_activity;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.StringRes;

import com.ashomok.lullabies.BuildConfig;
import com.ashomok.lullabies.R;
import com.ashomok.lullabies.ad.AdMobContainer;
import com.ashomok.lullabies.ad.AdMobContainerImpl;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class FullScreenPlayerModule {
    @Provides
    static Activity provideActivity(FullScreenPlayerActivity activity) {
        return activity;
    }

    @Provides
    static @StringRes
    int provideAdBannerId() {
        if (BuildConfig.DEBUG) {
            return R.string.test_banner;
        } else {
            return R.string.full_screen_activity_banner;
        }
    }
    @Provides
    static AdMobContainer provideAdMobContainer(Context context, @StringRes int adMobId) {
        return new AdMobContainerImpl(context, adMobId);
    }

    @Binds
    abstract FullScreenPlayerContract.Presenter mainPresenter(FullScreenPlayerPresenter presenter);
}