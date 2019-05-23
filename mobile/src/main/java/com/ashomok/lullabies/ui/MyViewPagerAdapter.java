package com.ashomok.lullabies.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ashomok.lullabies.AlbumArtCache;
import com.ashomok.lullabies.R;
import com.ashomok.lullabies.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;

import static com.ashomok.lullabies.utils.MediaItemStateHelper.*;

public class MyViewPagerAdapter extends PagerAdapter {
    private static final String TAG = LogHelper.makeLogTag(MyViewPagerAdapter.class);
    private Context context;

    //pager views by position
    private SparseArray<View> views;

    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private List<MediaBrowserCompat.MediaItem> mObjects;

    /**
     * Lock used to modify the content of {@link #mObjects}. Any write operation
     * performed on the array should be synchronized on this lock.
     */
    private final Object mLock = new Object();

    public MyViewPagerAdapter(Context context) {
        this.context = context;
        views = new SparseArray<>();
        mObjects = new ArrayList<>();
    }

    @Override
    public Object instantiateItem(@NonNull ViewGroup collection, int position) {
        if (sColorStateNotPlaying == null || sColorStatePlaying == null) {
            initializeColorStateLists(context);
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup convertView =
                (ViewGroup) inflater.inflate(R.layout.media_pager_item, collection, false);

        ImageView mBackgroundImage = convertView.findViewById(R.id.image);
        TextView textViewName = convertView.findViewById(R.id.name);
        TextView textViewGenre = convertView.findViewById(R.id.genre);

        MediaBrowserCompat.MediaItem mediaItem = mObjects.get(position);
        MediaDescriptionCompat description = mediaItem.getDescription();
        fetchImageAsync(description, mBackgroundImage);

        CharSequence name = description.getTitle();
        CharSequence category = description.getSubtitle();
        textViewName.setText(name);
        textViewGenre.setText(category);

        collection.addView(convertView);
        views.put(position, convertView);
        return convertView;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object view) {
        container.removeView((View) view);
        views.remove(position);
    }

    @Override
    public int getCount() {
        return this.mObjects.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void notifyDataSetChanged() {
        try {
            int key;
            for (int i = 0; i < views.size(); i++) {
                key = views.keyAt(i);
                View view = views.get(key);

                MediaBrowserCompat.MediaItem mediaItem = mObjects.get(key);
                // If the state of convertView is different, we need to adapt the view to the new state.

                int state = getMediaItemState((Activity) context, mediaItem);
                final ImageView tapMeImage = view.findViewById(R.id.tap_me_btn);
                if (state != STATE_PLAYING) {
                    tapMeImage.setVisibility(View.VISIBLE);
                } else {
                    tapMeImage.setVisibility(View.INVISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.notifyDataSetChanged();
    }

    public void add(MediaBrowserCompat.MediaItem item) {
        synchronized (mLock) {
            mObjects.add(item);
        }
        notifyDataSetChanged();
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        synchronized (mLock) {
            mObjects.clear();
            views.clear();
        }
        notifyDataSetChanged();
    }

    public MediaBrowserCompat.MediaItem getItem(int position) {
        return mObjects.get(position);
    }

    private void fetchImageAsync(
            @NonNull MediaDescriptionCompat description, ImageView mBackgroundImage) {
        if (description.getIconUri() == null) {
            return;
        }
        String artUrl = description.getIconUri().toString();
        AlbumArtCache cache = AlbumArtCache.getInstance();
        Bitmap art = cache.getBigImage(artUrl);
        if (art == null) {
            art = description.getIconBitmap();
        }
        if (art != null) {
            // if we have the art cached or from the MediaDescription, use it:
            mBackgroundImage.setImageBitmap(art);
        } else {
            // otherwise, fetch a high res version and update:
            cache.fetch(artUrl, new AlbumArtCache.FetchListener() {
                @Override
                public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                    mBackgroundImage.setImageBitmap(bitmap);
                }
            });
        }
    }
}