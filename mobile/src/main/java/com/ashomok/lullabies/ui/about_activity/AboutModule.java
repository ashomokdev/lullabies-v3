package com.ashomok.lullabies.ui.about_activity;

import android.app.Activity;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class AboutModule {
    @Provides
    static Activity provideActivity(AboutActivity activity) {
        return activity;
    }

    @Binds
    abstract AboutContract.Presenter mainPresenter(AboutPresenter presenter);
}
