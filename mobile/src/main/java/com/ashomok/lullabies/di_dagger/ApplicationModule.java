package com.ashomok.lullabies.di_dagger;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.ashomok.lullabies.R;
import com.ashomok.lullabies.billing_kotlin.viewmodels.BillingViewModel;
import com.ashomok.lullabies.ui.main_activity.MusicPlayerContract;
import com.ashomok.lullabies.ui.main_activity.MusicPlayerPresenter;
import com.ashomok.lullabies.utils.FirebaseAnalyticsHelper;
import com.ashomok.lullabies.utils.favourite_music.FavouriteMusicDAO;
import com.ashomok.lullabies.utils.rate_app.RateAppAsker;
import com.ashomok.lullabies.utils.rate_app.RateAppAskerImpl;

import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * This is a Dagger module. We use this to bind our Application class as a Context in the AppComponent
 * By using Dagger Android we do not need to pass our Application instance to any module,
 * we simply need to expose our Application as Context.
 * One of the advantages of Dagger.Android is that your
 * Application & Activities are provided into your graph for you.
 * {@link
 * AppComponent}.
 */
@Module
public abstract class ApplicationModule {
    @Provides
    static SharedPreferences provideSharedPrefs(Context context) {
        return context.getSharedPreferences( context.getString(R.string.preferences), Context.MODE_PRIVATE);
    }

    @Provides
    static FirebaseAnalyticsHelper provideFirebaseAnalyticsHelper(Context context) {
        return FirebaseAnalyticsHelper.getInstance(context);
    }

    @Provides
    static @NonNull
    FavouriteMusicDAO provideFavouriteMusicDAO(SharedPreferences sharedPreferences) {
        return FavouriteMusicDAO.getInstance(sharedPreferences);
    }

    @Binds
    abstract RateAppAsker provideRateAppAsker(RateAppAskerImpl rateAppAsker);


    @Binds
    abstract Context bindContext(Application application);

}

