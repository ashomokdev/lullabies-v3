package com.ashomok.lullabies.ui.about_activity;

import com.ashomok.lullabies.di_dagger.BasePresenter;

public class AboutContract {

    interface View {
        void showError(int res);
    }

    interface Presenter extends BasePresenter<AboutContract.View> {
        void openPrivacyPolicy();
    }
}
