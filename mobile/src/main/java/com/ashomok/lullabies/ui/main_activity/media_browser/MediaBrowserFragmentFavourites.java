package com.ashomok.lullabies.ui.main_activity.media_browser;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.media.session.MediaControllerCompat;

import com.ashomok.lullabies.utils.LogHelper;
import com.ashomok.lullabies.utils.favourite_music.FavouriteMusicDAO;

import javax.inject.Inject;

import static com.ashomok.lullabies.playback.PlaybackManager.CUSTOM_ACTION_CHANGE_FAVOURITE_STATE;
import static com.ashomok.lullabies.playback.PlaybackManager.CUSTOM_ACTION_FAVOURITES_LIST_OPENED;

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

        changeQueueToFavourites();
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        callback = null;
        favouriteMusicDAO.removeCallback();
        favouriteMusicDAO = null;
        LogHelper.d(TAG, "onDestroy, callback removed");
    }

    private void changeQueueToFavourites() {
        Activity activity = getActivity();
        if (activity != null){
            MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(activity);

            MediaControllerCompat.TransportControls controls = mediaController.getTransportControls();
            controls.sendCustomAction(CUSTOM_ACTION_FAVOURITES_LIST_OPENED,
                    mediaController.getPlaybackState().getExtras());
        }
    }
}
