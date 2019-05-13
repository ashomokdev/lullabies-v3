package com.example.android.uamp.ui;


import android.support.v4.media.MediaBrowserCompat;

/**
 * Created by Iuliia on 31.03.2016.
 */
public class FragmentFactory {

    public static MusicFragment newInstance(MediaBrowserCompat.MediaItem mediaItem) {
        return MusicFragment.newInstance(mediaItem);
    }
}
