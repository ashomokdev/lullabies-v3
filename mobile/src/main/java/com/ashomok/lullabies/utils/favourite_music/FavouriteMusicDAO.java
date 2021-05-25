package com.ashomok.lullabies.utils.favourite_music;

import android.content.SharedPreferences;
import android.telecom.Call;

import com.ashomok.lullabies.utils.LogHelper;
import com.ashomok.lullabies.utils.MediaItemStateHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FavouriteMusicDAO {

    private static final String TAG = LogHelper.makeLogTag(FavouriteMusicDAO.class);
    private final SharedPreferences mSharedPreferences;
    private final ConcurrentLinkedQueue<String> mFavoriteMusicIds;
    private static final String sharedPreferencesKey = "favourite_musics";
    private static FavouriteMusicDAO instance = null;
    private Callback callback;

    public static FavouriteMusicDAO getInstance(SharedPreferences sharedPreferences) {
        if (instance == null) {
            instance = new FavouriteMusicDAO(sharedPreferences);
        }
        return instance;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void removeCallback() {
        this.callback = null;
    }

    private FavouriteMusicDAO(SharedPreferences sharedPreferences) {
        mSharedPreferences = sharedPreferences;
        mFavoriteMusicIds = new ConcurrentLinkedQueue<>(
                mSharedPreferences.getStringSet(sharedPreferencesKey, new HashSet<>()));
    }

    public void addFavouriteMusic(String musicId) {
        if (!mFavoriteMusicIds.contains(musicId)) {
            mFavoriteMusicIds.add(musicId);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putStringSet(sharedPreferencesKey, new HashSet<>(mFavoriteMusicIds));
            LogHelper.d(TAG, "SharedPreferences updated with new data");
            editor.apply();
            if (callback != null) {
                callback.onFavouriteListUpdated();
            }
        }
    }

    public void removeFavouriteMusic(String musicId) {
        if (mFavoriteMusicIds.contains(musicId)) {
            mFavoriteMusicIds.remove(musicId);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putStringSet(sharedPreferencesKey, new HashSet<>(mFavoriteMusicIds));
            LogHelper.d(TAG, "SharedPreferences updated with new data");
            editor.apply();
            if (callback != null) {
                callback.onFavouriteListUpdated();
            }
        }
    }

    public void cleanFavouriteMusicList() {
        mFavoriteMusicIds.clear();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putStringSet(sharedPreferencesKey, new HashSet<>(mFavoriteMusicIds));
        LogHelper.d(TAG, "SharedPreferences updated with new data");
        editor.apply();
        if (callback != null) {
            callback.onFavouriteListUpdated();
        }
    }

    public Collection<String> getFavoriteMusicIds() {
        return mFavoriteMusicIds;
    }

    public boolean isFavouriteCollectionEmpty() {
        return !(mFavoriteMusicIds.size() > 0);
    }

    public boolean isFavorite(String musicId) {
        return mFavoriteMusicIds.contains(musicId);
    }

    public interface Callback {
        void onFavouriteListUpdated();
    }
}
