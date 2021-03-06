package com.ashomok.lullabies.ui.main_activity;

import android.support.v4.media.MediaBrowserCompat;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.ashomok.lullabies.billing_kotlin.localdb.AugmentedSkuDetails;
import com.ashomok.lullabies.di_dagger.BasePresenter;

import java.util.List;

public class MusicPlayerContract {

    interface View {

        void showError(@StringRes int errorMessageRes);

        void showInfo (@StringRes int infoMessageRes);

        void showInfo(String message);

        void updateViewForAd(boolean isAdsActive);

        void showRemoveAdDialog(AugmentedSkuDetails removeAdsSkuRow);

        AppCompatActivity getActivity();

        void addMenuItems(List<String> mediaIds);

        void checkForUserVisibleErrors(boolean forceError);
    }

    interface Presenter extends BasePresenter<View> {
        void onRemoveAdsClicked();

        void proposeRemoveAds();

        void rateApp();
    }
}
