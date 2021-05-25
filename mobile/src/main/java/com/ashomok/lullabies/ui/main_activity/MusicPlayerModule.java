package com.ashomok.lullabies.ui.main_activity;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.ashomok.lullabies.BuildConfig;
import com.ashomok.lullabies.R;
import com.ashomok.lullabies.ad.AdMobAd;
import com.ashomok.lullabies.ad.AdMobBannerAd;
import com.ashomok.lullabies.utils.LogHelper;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

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
        return activity.getMediaId(null, activity.getIntent());
    }

    @Provides
    static @StringRes
    int provideAdMobId(Context context, String mediaId) {
        if (BuildConfig.DEBUG) {
            return R.string.test_banner;
        } else {
            if (mediaId.contains(context.getResources().getString(R.string.classic_key))) {
                return R.string.lullabies_main_activity_classic_tones_banner;
            } else if (mediaId.contains(context.getResources().getString(R.string.mom_songs_key))) {
                return R.string.lullabies_main_activity_mom_songs_banner;
            } else {
                return R.string.lullabies_main_activity_base_collection_banner;
            }
        }
    }

    @Provides
    static AdMobAd provideAdMobAd(Activity activity, @StringRes int adMobId) {
        LogHelper.d(TAG, adMobId);
        return new AdMobBannerAd(activity, adMobId);
    }

    @Binds
    abstract MusicPlayerContract.Presenter mainPresenter(MusicPlayerPresenter presenter);
}
