package com.example.android.uamp.ui.music_player_activity;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.example.android.uamp.billing.model.SkuRowData;
import com.example.android.uamp.di_dagger.BasePresenter;

import java.util.List;

public class MusicPlayerContract {

    interface View {

        void showRemoveAdDialog(SkuRowData data);

        void updateView(boolean isAdsActive);

    }

    interface Presenter extends BasePresenter<View> {
        void onRemoveAdsClicked();

        void proposeRemoveAds();
    }
}
