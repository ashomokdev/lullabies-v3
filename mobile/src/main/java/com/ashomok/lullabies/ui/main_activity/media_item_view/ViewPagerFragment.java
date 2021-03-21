package com.ashomok.lullabies.ui.main_activity.media_item_view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ashomok.lullabies.AlbumArtCache;
import com.ashomok.lullabies.R;
import com.ashomok.lullabies.utils.FirebaseAnalyticsHelper;
import com.ashomok.lullabies.utils.LogHelper;

import static com.ashomok.lullabies.utils.MediaItemStateHelper.STATE_PLAYING;
import static com.ashomok.lullabies.utils.MediaItemStateHelper.getMediaItemState;

public class ViewPagerFragment extends Fragment {
    private static final String TAG = LogHelper.makeLogTag(ViewPagerFragment.class);
    private static final String ARG_MEDIA_ID = "media_id";
    private ImageView tapMeImage;
    private MediaBrowserCompat.MediaItem mediaItem;

    private final MediaControllerCompat.Callback callback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            updateTapMeButtonVisibility();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View convertView = inflater.inflate(
                R.layout.fragment_media_item, container, false);


        ImageView mBackgroundImage = convertView.findViewById(R.id.image);
        TextView textViewName = convertView.findViewById(R.id.name);
        TextView textViewGenre = convertView.findViewById(R.id.genre);
        tapMeImage = convertView.findViewById(R.id.tap_me_img);

        mediaItem = getMediaItem();
        MediaDescriptionCompat description = mediaItem.getDescription();
        fetchImageAsync(description, mBackgroundImage);

        CharSequence name = description.getTitle();
        CharSequence category = description.getSubtitle();
        textViewName.setText(name);
        textViewGenre.setText(category);

        Activity activity = getActivity();

        convertView.setOnClickListener(v -> {
            MediaBrowserCompat.MediaItem item = getMediaItem();
            LogHelper.d(TAG, "onMediaItemSelected, mediaId=" + item.getDescription().getTitle());
            FirebaseAnalyticsHelper.getInstance(getContext()).trackContentSelected(item);
            if (item.isPlayable() && activity != null) {
                MediaControllerCompat.getMediaController(activity).getTransportControls()
                        .playFromMediaId(item.getMediaId(), null);
            }
        });
        return convertView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateTapMeButtonVisibility();
        Activity activity = getActivity();
        if (activity != null){
            MediaControllerCompat.getMediaController(activity).registerCallback(callback);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Activity activity = getActivity();
        if (activity != null){
            MediaControllerCompat.getMediaController(activity).unregisterCallback(callback);
        }
    }

    public MediaBrowserCompat.MediaItem getMediaItem() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getParcelable(ARG_MEDIA_ID);
        }
        return null;
    }

    public void setMediaItem(MediaBrowserCompat.MediaItem mediaItem) {
        Bundle args = new Bundle(1);
        args.putParcelable(ARG_MEDIA_ID, mediaItem);
        setArguments(args);
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

            //set placeholder
            ColorDrawable cd = new ColorDrawable(getResources().getColor(R.color.colorPrimarySecond));
            mBackgroundImage.setImageDrawable(cd);
            cache.fetch(artUrl, new AlbumArtCache.FetchListener() {

                @Override
                public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                    mBackgroundImage.setImageBitmap(bitmap);
                }
            });
        }
    }

    private void updateTapMeButtonVisibility() {
        Activity activity = getActivity();
        if (activity != null) {
            int stateForThisMediaItem = getMediaItemState(activity, mediaItem);
            if (stateForThisMediaItem == STATE_PLAYING) {
                tapMeImage.setVisibility(View.GONE);
            } else {
                tapMeImage.setVisibility(View.VISIBLE);
            }
            LogHelper.d(TAG, mediaItem.getDescription().getTitle()
                    + " playing: " + (stateForThisMediaItem == STATE_PLAYING), ", state: "
                    + stateForThisMediaItem);
        }
    }
}