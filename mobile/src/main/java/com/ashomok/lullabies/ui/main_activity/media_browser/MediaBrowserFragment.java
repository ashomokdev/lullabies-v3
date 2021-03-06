/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ashomok.lullabies.ui.main_activity.media_browser;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.ashomok.lullabies.R;
import com.ashomok.lullabies.music_service.MusicService;
import com.ashomok.lullabies.tools.CirclesViewPagerPageIndicatorView;
import com.ashomok.lullabies.ui.main_activity.media_item_view.MyViewPagerAdapter;
import com.ashomok.lullabies.utils.LogHelper;
import com.ashomok.lullabies.utils.MediaIDHelper;
import com.ashomok.lullabies.utils.Result;

import java.util.List;

import dagger.android.support.DaggerFragment;
import kotlin.Unit;

import static android.view.View.VISIBLE;

/**
 * A Fragment that lists all the various browsable queues available
 * from a {@link android.service.media.MediaBrowserService}.
 * <p/>
 * It uses a {@link MediaBrowserCompat} to connect to the {@link MusicService}.
 * Once connected, the fragment subscribes to get all the children.
 * All {@link MediaBrowserCompat.MediaItem}'s that can be browsed are shown in a ListView.
 */
public class MediaBrowserFragment extends DaggerFragment {

    private static final String TAG = LogHelper.makeLogTag(MediaBrowserFragment.class);

    private static final String ARG_MEDIA_ID = "media_id";

    String mMediaId;
    MediaFragmentListener mMediaFragmentListener;

    private View emptyResultView;
    private TextView mErrorMessage;
    private CirclesViewPagerPageIndicatorView circlesViewPagerPageIndicatorView;
    private ViewPager2 viewPager;
    private ProgressBar progressBar;
    private MyViewPagerAdapter mBrowserAdapter;

    // Receive callbacks from the MediaController. Here we update our state such as which queue
    // is being shown, the current title and description and the PlaybackState.
    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    if (metadata == null) {
                        return;
                    }
                    LogHelper.d(TAG, "Received metadata change to media ",
                            metadata.getDescription().getMediaId());
                    mBrowserAdapter.notifyDataSetChanged();
                }

                @Override
                public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
                    LogHelper.d(TAG, "Received state change: ", state);
                    mBrowserAdapter.notifyDataSetChanged();
                    updateLoadingView(state);
                }
            };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // If used on an activity that doesn't implement MediaFragmentListener, it
        // will throw an exception as expected:
        mMediaFragmentListener = (MediaFragmentListener) activity;
        mBrowserAdapter = new MyViewPagerAdapter(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMediaFragmentListener = null;
        mBrowserAdapter = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogHelper.d(TAG, "fragment.onCreateView");
        View rootView = inflater.inflate(R.layout.media_browser_fragment, container, false);

        progressBar = rootView.findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);

        emptyResultView = rootView.findViewById(R.id.empty_result_layout);
        mErrorMessage = emptyResultView.findViewById(R.id.error_message);

        //init pager
        viewPager = rootView.findViewById(R.id.pager);
        viewPager.setAdapter(mBrowserAdapter);

        //todo possible to add favourite icon here for each mediaid

        circlesViewPagerPageIndicatorView = rootView.findViewById(R.id.circle_view);
        circlesViewPagerPageIndicatorView.setColorAccent(getResources().getColor(R.color.colorAccent));
        circlesViewPagerPageIndicatorView.setColorBase(getResources().getColor(R.color.colorPrimary));
        circlesViewPagerPageIndicatorView.setViewPager(viewPager);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // fetch browsing information to fill the listview:
        MediaBrowserCompat mediaBrowser = mMediaFragmentListener.getMediaBrowser();

        LogHelper.d(TAG, "fragment.onStart, mediaId=", mMediaId,
                "  onConnected=" + mediaBrowser.isConnected());

        if (mediaBrowser.isConnected()) {
            onConnected();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        MediaBrowserCompat mediaBrowser = mMediaFragmentListener.getMediaBrowser();
        if (mediaBrowser != null && mediaBrowser.isConnected() && mMediaId != null) {
            mediaBrowser.unsubscribe(mMediaId);
        }
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            controller.unregisterCallback(mMediaControllerCallback);
            LogHelper.d(TAG, "unregister Callback");
        }
    }

    public String getMediaId() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getString(ARG_MEDIA_ID);
        }
        return null;
    }

    public void setMediaId(String mediaId) {
        Bundle args = new Bundle(1);
        args.putString(ARG_MEDIA_ID, mediaId);
        setArguments(args);
    }

    // Called when the MediaBrowser is connected. This method is either called by the
    // fragment.onStart() or explicitly by the activity in the case where the connection
    // completes after the onStart()
    public void onConnected() {
        LogHelper.d(TAG, "onConnected");
        if (isDetached()) {
            return;
        }
        mMediaId = getMediaId();
        if (mMediaId == null) {
            mMediaId = mMediaFragmentListener.getMediaBrowser().getRoot();
        }
        setToolbarTitle(mMediaId);

        MediaBrowserLoader.loadChildrenMediaItems(
                mMediaFragmentListener.getMediaBrowser(), mMediaId, this::fillAdapter);

        // Add MediaController callback so we can redraw the view when metadata changes:
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            controller.registerCallback(mMediaControllerCallback);
            LogHelper.d(TAG, "register Callback");
            PlaybackStateCompat state = controller.getPlaybackState();
            updateLoadingView(state);
        }
    }

    @Nullable
    Unit fillAdapter(
            Result<? extends List<? extends MediaBrowserCompat.MediaItem>> result) {
        if (mBrowserAdapter == null) {
            LogHelper.e(TAG, "mBrowserAdapter == null - unexpected");
        } else {
            mBrowserAdapter.clear();

            if (result instanceof Result.Success) {
                List<MediaBrowserCompat.MediaItem> mediaItems =
                        ((Result.Success<List<MediaBrowserCompat.MediaItem>>) result).getData();
                if (mediaItems.isEmpty()) {
                    checkForUserVisibleErrors(true, R.string.empty_collection);
                } else {
                    for (MediaBrowserCompat.MediaItem item : mediaItems) {
                        mBrowserAdapter.add(item);
                    }
                    mBrowserAdapter.notifyDataSetChanged();
                }

            } else if (result instanceof Result.Error) {
                LogHelper.e(TAG, ((Result.Error) result).getException(), "Error from loading media");
                checkForUserVisibleErrors(true);
            } else {
                LogHelper.e(TAG, "Unknown error, unexpected result.");
            }
        }
        progressBar.setVisibility(View.GONE);
        return null;
    }

    private void checkForUserVisibleErrors(boolean forceError, int errorMessageResId) {
        boolean showError = forceError;

        //if state is ERROR and metadata!=null, use playback state error message:
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null
                && controller.getMetadata() != null
                && controller.getPlaybackState() != null
                && controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_ERROR
                && controller.getPlaybackState().getErrorMessage() != null) {
            mErrorMessage.setText(controller.getPlaybackState().getErrorMessage());
            showError = true;
        } else if (forceError) {
            // Finally, if the caller requested to show error, show a generic message:
            mErrorMessage.setText(errorMessageResId);
            showError = true;
        }
        emptyResultView.setVisibility(showError ? View.VISIBLE : View.INVISIBLE);
    }

    private void checkForUserVisibleErrors(boolean forceError) {
        checkForUserVisibleErrors(forceError, R.string.error_loading_media);
    }

    public ViewPager2 getViewPager() {
        return viewPager;
    }

    private void updateLoadingView(PlaybackStateCompat state) {
        LogHelper.d(TAG, "updateLoadingView with state " + state);
        switch (state.getState()) {
            case PlaybackStateCompat.STATE_BUFFERING:
                progressBar.setVisibility(VISIBLE);
                break;
            default:
                progressBar.setVisibility(View.GONE);
                break;
        }
    }

    private void setToolbarTitle(String mediaId) {
        String appName = getString(R.string.app_name);

        String title = MediaIDHelper.extractBrowseCategoryValueFromMediaID(mediaId);
        if (title != null && !title.isEmpty()) {
            mMediaFragmentListener.setToolbarTitle(appName + " - " + title);
        } else {
            mMediaFragmentListener.setToolbarTitle(appName);
        }
    }
}
