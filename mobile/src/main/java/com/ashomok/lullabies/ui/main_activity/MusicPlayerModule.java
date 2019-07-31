package com.ashomok.lullabies.ui.main_activity;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.ashomok.lullabies.BuildConfig;
import com.ashomok.lullabies.R;
import com.ashomok.lullabies.ad.AdMobContainer;
import com.ashomok.lullabies.ad.AdMobContainerImpl;
import com.ashomok.lullabies.billing.BillingProvider;
import com.ashomok.lullabies.billing.BillingProviderImpl;
import com.ashomok.lullabies.billing_kotlin.viewmodels.BillingViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MusicPlayerModule {

    @Provides
    static AppCompatActivity provideActivity(MusicPlayerActivity activity) {
        return activity;
    }

//    @Provides
//    static BillingProvider provideBillingProvider(MusicPlayerActivity activity) {
//        return new BillingProviderImpl(activity);
//    }

//    @Provides
//    static BillingViewModel provideBillingViewModel(MusicPlayerActivity activity) {
//        return ViewModelProviders.of(activity).get(BillingViewModel.class);
//    }


    @Provides
    static @StringRes
    int provideAdBannerId() {
        if (BuildConfig.DEBUG) {
            return R.string.test_banner;
        } else {
            return R.string.main_activity_banner;
        }
    }

    @Provides
    static AdMobContainer provideAdMobContainer(Context context, @StringRes int adMobId) {
       return new AdMobContainerImpl(context, adMobId);
    }

    @Binds
    abstract MusicPlayerContract.Presenter mainPresenter(MusicPlayerPresenter presenter);

    @ContributesAndroidInjector
    abstract MediaBrowserFragment mediaBrowserFragment();
}
