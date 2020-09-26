package com.ashomok.lullabies.ui.full_screen_player_activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.media.session.MediaControllerCompat;

import androidx.annotation.Nullable;

import com.ashomok.lullabies.ui.main_activity.MusicPlayerPresenter;
import com.ashomok.lullabies.utils.LogHelper;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;

public class FullScreenPlayerPresenter implements FullScreenPlayerContract.Presenter {
    public static final String TAG = LogHelper.makeLogTag(MusicPlayerPresenter.class);

    @Nullable
    public FullScreenPlayerContract.View view;

    private Context context;
    private SharedPreferences sharedPreferences;
    private LinkedHashSet<String> mSavedFavouriteMusics;
    private static final String sharedPreferencesKey = "favourite_musics";

    /**
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     */
    @Inject
    FullScreenPlayerPresenter(Context context, SharedPreferences sharedPreferences) {
        this.context = context;
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public void takeView(FullScreenPlayerContract.View activity) {
        view = activity;
        init();
    }

    private void init() {
        mSavedFavouriteMusics = new LinkedHashSet<>(
                sharedPreferences.getStringSet(sharedPreferencesKey, new HashSet<>()));
    }

    @Override
    public void dropView() {
        view = null;
    }

    @Override
    public void addFavouriteMusicToSharedPreferences(String mediaId) {
        //todo add synchronize keyword fot thread save
        LinkedHashSet<String> savedFavouriteMusicsUpdated = new LinkedHashSet<>();
        savedFavouriteMusicsUpdated.add(mediaId);

        if (!mSavedFavouriteMusics.equals(savedFavouriteMusicsUpdated)) {
            mSavedFavouriteMusics = savedFavouriteMusicsUpdated;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet(sharedPreferencesKey, new HashSet<>(mSavedFavouriteMusics));
            LogHelper.d(TAG, "SharedPreferences updated with new data");
            editor.apply();
        }
    }

    @Override
    public void removeFavouriteMusicToSharedPreferences(String mediaId) {
        //todo add synchronize keyword fot thread save
        LinkedHashSet<String> savedFavouriteMusicsUpdated = new LinkedHashSet<>();
        savedFavouriteMusicsUpdated.remove(mediaId);

        if (!mSavedFavouriteMusics.equals(savedFavouriteMusicsUpdated)) {
            mSavedFavouriteMusics = savedFavouriteMusicsUpdated;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet(sharedPreferencesKey, new HashSet<>(mSavedFavouriteMusics));
            LogHelper.d(TAG, "SharedPreferences updated with new data");
            editor.apply();
        }
    }
}
