package com.ashomok.lullabies.ui.about_activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.ashomok.lullabies.BuildConfig;
import com.ashomok.lullabies.R;
import com.ashomok.lullabies.utils.LogHelper;
import com.ashomok.lullabies.utils.NetworkHelper;
import com.ashomok.lullabies.utils.rate_app.RateAppUtil;

import javax.inject.Inject;

public class AboutPresenter implements AboutContract.Presenter {
    public static final String TAG = LogHelper.makeLogTag(AboutPresenter.class);

    @Nullable
    public AboutContract.View view;

    private Context context;


    /**
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     */
    @Inject
    AboutPresenter(Context context) {
        this.context = context;

    }

    @Override
    public void takeView(AboutContract.View activity) {
        view = activity;
        init();
    }

    private void init() {
    }

    @Override
    public void dropView() {
        view = null;
    }

    @Override
    public void openPrivacyPolicy() {
        checkConnection();
        if (NetworkHelper.isOnline((Activity) view)) {
            ((Activity) view).startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.PRIVACY_POLICY_LINK)));
        }
    }

    private void checkConnection() {
        if (view != null ) {
            if (!NetworkHelper.isOnline((Activity) view)) {
                view.showError(R.string.no_internet_connection);
            }
        }
    }
}
