package com.ashomok.lullabies.ui.main_activity.media_browser;

import android.os.Bundle;

import com.ashomok.lullabies.utils.LogHelper;
import com.ashomok.lullabies.utils.favourite_music.FavouriteMusicDAO;

import javax.inject.Inject;

public class MediaBrowserFragmentFavourites extends MediaBrowserFragment {
    private static final String TAG = LogHelper.makeLogTag(MediaBrowserFragmentFavourites.class);

    @Inject
    FavouriteMusicDAO favouriteMusicDAO;

    FavouriteMusicDAO.Callback callback = () -> {
        MediaBrowserLoader.loadChildrenMediaItems(
                mMediaFragmentListener.getMediaBrowser(), mMediaId, this::fillAdapter);

        LogHelper.d(TAG, "onFavouriteListUpdated, adapter refilled");
    };

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogHelper.d(TAG, "onCreate, callback added");
        favouriteMusicDAO.setCallback(callback);
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        callback = null;
        favouriteMusicDAO.removeCallback();
        favouriteMusicDAO = null;
        LogHelper.d(TAG, "onDestroy, callback removed");
    }
}
