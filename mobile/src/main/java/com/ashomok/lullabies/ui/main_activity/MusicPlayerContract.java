package com.ashomok.lullabies.ui.main_activity;

import android.support.annotation.StringRes;

import com.ashomok.lullabies.billing.model.SkuRowData;
import com.ashomok.lullabies.di_dagger.BasePresenter;

public class MusicPlayerContract {

    interface View {

        void showError(@StringRes int errorMessageRes);

        void showInfo (@StringRes int infoMessageRes);

        void showInfo(String message);

        void showRemoveAdDialog(SkuRowData data);

        void updateView(boolean isAdsActive);
    }

    interface Presenter extends BasePresenter<View> {
        void onRemoveAdsClicked();

        void proposeRemoveAds();
    }
}
