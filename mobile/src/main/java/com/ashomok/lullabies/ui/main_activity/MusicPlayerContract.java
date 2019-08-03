package com.ashomok.lullabies.ui.main_activity;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.ashomok.lullabies.billing_kotlin.localdb.AugmentedSkuDetails;
import com.ashomok.lullabies.di_dagger.BasePresenter;

public class MusicPlayerContract {

    interface View {

        void showError(@StringRes int errorMessageRes);

        void showInfo (@StringRes int infoMessageRes);

        void showInfo(String message);

        void updateView(boolean isAdsActive);

        void showRemoveAdDialog(AugmentedSkuDetails removeAdsSkuRow);

        AppCompatActivity getActivity();
    }

    interface Presenter extends BasePresenter<View> {
        void onRemoveAdsClicked();

        void proposeRemoveAds();
    }
}
