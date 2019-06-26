package com.ashomok.lullabies.ui.about_activity;

import android.content.Context;
import androidx.annotation.Nullable;

import com.ashomok.lullabies.utils.LogHelper;

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
}
