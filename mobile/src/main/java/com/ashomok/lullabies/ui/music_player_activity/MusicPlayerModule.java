package com.ashomok.lullabies.ui.music_player_activity;

import android.app.Activity;
import android.support.annotation.StringRes;

import com.ashomok.lullabies.BuildConfig;
import com.ashomok.lullabies.R;

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

    @Binds
    abstract MusicPlayerContract.Presenter mainPresenter(MusicPlayerPresenter presenter);
}
