package com.ashomok.lullabies;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.media.MediaDescription;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.content.ContextCompat;

public class ServiceManager {

    private final MediaNotificationManager mMediaNotificationManager;


    public ServiceManager (MediaNotificationManager mMediaNotificationManager){
        this.mMediaNotificationManager = mMediaNotificationManager;
    }


    public void moveServiceToStartedState(PlaybackStateCompat state,
                                          MediaDescriptionCompat mediaDescriptionCompat) {
        Notification notification =
                mMediaNotificationManager.getNotification(
                        mediaDescriptionCompat, state,
                        mMediaNotificationManager.getmService().getSessionToken());

        if (!mMediaNotificationManager.getmService().getInStartedState()) {
            Context context = mMediaNotificationManager.getmService();
            ContextCompat.startForegroundService(context, new Intent(context, MusicService.class));
            mMediaNotificationManager.getmService().setInStartedState(true);
        }

        mMediaNotificationManager.getmService()
                .startForeground(MediaNotificationManager.NOTIFICATION_ID, notification);
    }


    public void updateNotificationForPause(PlaybackStateCompat state,
                                           MediaDescriptionCompat mediaDescriptionCompat) {
        mMediaNotificationManager.getmService().stopForeground(false);
        Notification notification =
                mMediaNotificationManager.getNotification(
                        mediaDescriptionCompat, state,
                        mMediaNotificationManager.getmService().getSessionToken());
        mMediaNotificationManager.getmNotificationManager()
                .notify(MediaNotificationManager.NOTIFICATION_ID, notification);
    }


    public void moveServiceOutOfStartedState() {
        mMediaNotificationManager.getmService().stopForeground(true);
        mMediaNotificationManager.getmService().stopSelf();
        mMediaNotificationManager.getmService().setInStartedState(false);
    }
}
