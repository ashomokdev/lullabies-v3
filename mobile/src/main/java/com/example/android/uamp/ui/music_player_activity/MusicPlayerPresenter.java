package com.example.android.uamp.ui.music_player_activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.example.android.uamp.utils.LogHelper;

import javax.inject.Inject;

public class MusicPlayerPresenter implements MusicPlayerContract.Presenter {
    public static final String TAG = LogHelper.makeLogTag(MusicPlayerPresenter.class);

    @Nullable
    public MusicPlayerContract.View view;

    private Context context;

    /**
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     */
    @Inject
    MusicPlayerPresenter(Context context) {
        this.context = context;
    }

    @Override
    public void onRemoveAdsClicked() {

    }

    @Override
    public void proposeRemoveAds() {

    }

    @Override
    public void takeView(MusicPlayerContract.View view) {

    }

    @Override
    public void dropView() {

    }
}
