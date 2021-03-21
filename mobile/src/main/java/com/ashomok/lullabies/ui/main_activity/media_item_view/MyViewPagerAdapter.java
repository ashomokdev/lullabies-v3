package com.ashomok.lullabies.ui.main_activity.media_item_view;

import android.support.v4.media.MediaBrowserCompat;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.annimon.stream.Stream;
import com.ashomok.lullabies.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

public class MyViewPagerAdapter extends FragmentStateAdapter {
    private static final String TAG = LogHelper.makeLogTag(MyViewPagerAdapter.class);

    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private final List<MediaBrowserCompat.MediaItem> mediaItems;

    /**
     * Lock used to modify the content of {@link #mediaItems}. Any write operation
     * performed on the array should be synchronized on this lock.
     */
    private final Object mLock = new Object();

    public MyViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
        mediaItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        ViewPagerFragment fragment = new ViewPagerFragment();
        fragment.setMediaItem(mediaItems.get(position));
        return fragment;
    }

    @Override
    public long getItemId(int position) {
        return mediaItems.get(position).getMediaId().hashCode();
    }

    @Override
    public boolean containsItem(long itemId) {
        return Stream.of(mediaItems).map(t -> (long)t.getMediaId().hashCode())
                .anyMatch(t -> t.equals((Long) itemId));
    }

    @Override
    public int getItemCount() {
        return mediaItems.size();
    }

    public void add(MediaBrowserCompat.MediaItem item) {
        synchronized (mLock) {
            mediaItems.add(item);
        }
    }

    public void clear() {
        synchronized (mLock) {
            mediaItems.clear();
        }
    }
}