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

package com.ashomok.lullabies;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.RemoteException;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import androidx.media.session.MediaButtonReceiver;

import com.ashomok.lullabies.ui.main_activity.MusicPlayerActivity;
import com.ashomok.lullabies.utils.LogHelper;
import com.ashomok.lullabies.utils.ResourceHelper;
import com.google.android.gms.cast.framework.CastContext;

/**
 * Keeps track of a notification and updates it automatically for a given
 * MediaSession. Maintaining a visible notification (usually) guarantees that the music service
 * won't be killed during playback.
 */

//public class MediaNotificationManager extends BroadcastReceiver {

public class MediaNotificationManager extends BroadcastReceiver {
    private static final String TAG = LogHelper.makeLogTag(MediaNotificationManager.class);

    private static final String CHANNEL_ID = "com.example.uamp.MUSIC_CHANNEL_ID";

    public static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;

    public static final String ACTION_PAUSE = "com.example.uamp.pause";
    public static final String ACTION_PLAY = "com.example.uamp.play";
    public static final String ACTION_PREV = "com.example.uamp.prev";
    public static final String ACTION_NEXT = "com.example.uamp.next";
    public static final String ACTION_STOP = "com.example.uamp.stop";
    public static final String ACTION_STOP_CASTING = "com.example.uamp.stop_cast";

    public MusicService getmService() {
        return mService;
    }

    private MusicService mService;
    private MediaSessionCompat.Token mSessionToken;
    private MediaControllerCompat mController;
    private MediaControllerCompat.TransportControls mTransportControls;

    private PlaybackStateCompat mPlaybackState;
    private MediaMetadataCompat mMetadata;

    public NotificationManager getmNotificationManager() {
        return mNotificationManager;
    }

    private final NotificationManager mNotificationManager;

    private final PendingIntent mPlayIntent;
    private final PendingIntent mPauseIntent;
    private final PendingIntent mPreviousIntent;
    private final PendingIntent mNextIntent;
    private final PendingIntent mStopIntent;

    private final PendingIntent mStopCastIntent;

    private final int mNotificationColor;

    public MediaNotificationManager(MusicService service) throws RemoteException {
        mService = service;
        updateSessionToken();

        mNotificationColor = ResourceHelper.getThemeColor(mService, R.attr.colorPrimary,
                Color.DKGRAY);

        mNotificationManager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);

        String pkg = mService.getPackageName();
        mPauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPreviousIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mNextIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mStopIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_STOP).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mStopCastIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_STOP_CASTING).setPackage(pkg),
                PendingIntent.FLAG_CANCEL_CURRENT);

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        mNotificationManager.cancelAll();
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        LogHelper.d(TAG, "Received intent with action " + action);
        switch (action) {
            case ACTION_PAUSE:
                mTransportControls.pause();
                break;
            case ACTION_PLAY:
                mTransportControls.play();
                break;
            case ACTION_NEXT:
                mTransportControls.skipToNext();
                break;
            case ACTION_PREV:
                mTransportControls.skipToPrevious();
                break;
            case ACTION_STOP_CASTING:
                CastContext.getSharedInstance(mService).getSessionManager().endCurrentSession(true);
                break;
            default:
                LogHelper.w(TAG, "Unknown intent ignored. Action=", action);
        }
    }

    /**
     * Update the state based on a change on the session token. Called either when
     * we are running for the first time or when the media session owner has destroyed the session
     * (see {@link android.media.session.MediaController.Callback#onSessionDestroyed()})
     */
    private void updateSessionToken() throws RemoteException {
        MediaSessionCompat.Token freshToken = mService.getSessionToken();
        if (mSessionToken == null && freshToken != null ||
                mSessionToken != null && !mSessionToken.equals(freshToken)) {
            mSessionToken = freshToken;
            if (mSessionToken != null) {
                mController = new MediaControllerCompat(mService, mSessionToken);
                mTransportControls = mController.getTransportControls();
            }
        }
    }

    private PendingIntent createContentIntent(MediaDescriptionCompat description) {
        Intent openUI = new Intent(mService, MusicPlayerActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openUI.putExtra(MusicPlayerActivity.EXTRA_START_FULLSCREEN, true);
        if (description != null) {
            openUI.putExtra(MusicPlayerActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION, description);
        }
        return PendingIntent.getActivity(mService, REQUEST_CODE, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private int addActions(
            final NotificationCompat.Builder notificationBuilder,
            @NonNull PlaybackStateCompat state) {
        LogHelper.d(TAG, "updatePlayPauseAction");

        int playPauseButtonPosition = 0;
        // If skip to previous action is enabled
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            notificationBuilder.addAction(R.drawable.ic_skip_previous_white_24dp,
                    mService.getString(R.string.label_previous), mPreviousIntent);

            // If there is a "skip to previous" button, the play/pause button will
            // be the second one. We need to keep track of it, because the MediaStyle notification
            // requires to specify the index of the buttons (actions) that should be visible
            // when in compact view.
            playPauseButtonPosition = 1;
        }

        // Play or pause button, depending on the current state.
        final String label;
        final int icon;
        final PendingIntent intent;
        if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
            label = mService.getString(R.string.label_pause);
            icon = R.drawable.uamp_ic_pause_white_24dp;
            intent = mPauseIntent;
        } else {
            label = mService.getString(R.string.label_play);
            icon = R.drawable.uamp_ic_play_arrow_white_24dp;
            intent = mPlayIntent;
        }
        notificationBuilder.addAction(new NotificationCompat.Action(icon, label, intent));

        // If skip to next action is enabled
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
            notificationBuilder.addAction(R.drawable.ic_skip_next_white_24dp,
                    mService.getString(R.string.label_next), mNextIntent);
        }

        return playPauseButtonPosition;
    }

    private void fetchBitmapFromURLAsync(final String bitmapUrl,
                                         final NotificationCompat.Builder builder, PlaybackStateCompat state) {
        AlbumArtCache.getInstance().fetch(bitmapUrl, new AlbumArtCache.FetchListener() {
            @Override
            public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                if (mMetadata != null && mMetadata.getDescription().getIconUri() != null &&
                        mMetadata.getDescription().getIconUri().toString().equals(artUrl)) {
                    // If the media is still the same, update the notification:
                    LogHelper.d(TAG, "fetchBitmapFromURLAsync: set bitmap to ", artUrl);
                    builder.setLargeIcon(bitmap);
                    addActions(builder, state);
                    mNotificationManager.notify(NOTIFICATION_ID, builder.build());
                }
            }
        });
    }

    /**
     * Creates Notification Channel. This is required in Android O+ to display notifications.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            // The user-visible name of the channel.
            CharSequence name = mService.getString(R.string.notification_channel);
            // The user-visible description of the channel.
            String description = mService.getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(
                    new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
            LogHelper.d(TAG, "createChannel: New channel created");
        } else {
            LogHelper.d(TAG, "createChannel: Existing channel reused");
        }
    }

    public Notification getNotification(MediaDescriptionCompat description,
                                        PlaybackStateCompat state,
                                        MediaSessionCompat.Token sessionToken) {
        NotificationCompat.Builder builder =
                buildNotification(state, sessionToken, description);

        Notification notification = builder.build();

        if (notification != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_NEXT);
            filter.addAction(ACTION_PAUSE);
            filter.addAction(ACTION_PLAY);
            filter.addAction(ACTION_PREV);
            filter.addAction(ACTION_STOP_CASTING);
            mService.registerReceiver(this, filter);
        }

        return notification;
    }

    private NotificationCompat.Builder buildNotification(PlaybackStateCompat state,
                                                         MediaSessionCompat.Token sessionToken,
                                                         MediaDescriptionCompat description) {

        String fetchArtUrl = null;
        Bitmap art = null;
        if (description.getIconUri() != null) {
            // This sample assumes the iconUri will be a valid URL formatted String, but
            // it can actually be any valid Android Uri formatted String.
            // async fetch the album art icon
            String artUrl = description.getIconUri().toString();
            art = AlbumArtCache.getInstance().getBigImage(artUrl);
            if (art == null) {
                fetchArtUrl = artUrl;
                // use a placeholder art while the remote art is being downloaded
                art = BitmapFactory.decodeResource(mService.getResources(),
                        R.drawable.ic_default_art);
            }
        }

        // Notification channels are only supported on Android O+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mService, CHANNEL_ID);
        final int playPauseButtonPosition = addActions(builder, state);

        builder.setStyle(
                new MediaStyle()
                        .setMediaSession(sessionToken)
                        .setShowActionsInCompactView(playPauseButtonPosition)
                        // For backwards compatibility with Android L and earlier.
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(mStopIntent))
                .setColor(mNotificationColor)
                .setSmallIcon(R.drawable.ic_notification)
                // Pending intent that is fired when user clicks on notification.
                .setContentIntent(createContentIntent(description))
                // Title - Usually Song name.
                .setContentTitle(description.getTitle())
                // Subtitle - Usually Artist name.
                .setContentText(description.getSubtitle())
                .setLargeIcon(art)
                // When notification is deleted (when playback is paused and notification can be
                // deleted) fire MediaButtonPendingIntent with ACTION_STOP.
                .setDeleteIntent(mStopIntent)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (mController != null && mController.getExtras() != null) {
            String castName = mController.getExtras().getString(MusicService.EXTRA_CONNECTED_CAST);
            if (castName != null) {
                String castInfo = mService.getResources()
                        .getString(R.string.casting_to_device, castName);
                builder.setSubText(castInfo);
                builder.addAction(R.drawable.ic_close_black_24dp,
                        mService.getString(R.string.stop_casting), mStopCastIntent);
            }
        }


        if (fetchArtUrl != null) {
            fetchBitmapFromURLAsync(fetchArtUrl, builder, state);
        }
        return builder;
    }

    public void unregisterReceiver() {
        try {
            mNotificationManager.cancel(NOTIFICATION_ID);
            mService.unregisterReceiver(this);
        } catch (IllegalArgumentException ex) {
            // ignore if the receiver is not registered.
        }
    }
}
