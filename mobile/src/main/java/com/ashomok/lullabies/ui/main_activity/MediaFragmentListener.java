package com.ashomok.lullabies.ui.main_activity;

import android.support.v4.media.MediaBrowserCompat;

import com.ashomok.lullabies.ui.MediaBrowserProvider;

public interface MediaFragmentListener extends MediaBrowserProvider {
    void onMediaItemSelected(MediaBrowserCompat.MediaItem item);
    void setTitle(CharSequence title);
}