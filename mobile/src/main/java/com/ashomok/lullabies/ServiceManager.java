package com.ashomok.lullabies;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.content.ContextCompat;

import com.ashomok.lullabies.utils.LogHelper;


public class ServiceManager {

    private static final String TAG = LogHelper.makeLogTag(ServiceManager.class);

    private final MediaNotificationManager mMediaNotificationManager;
    private MusicService musicService;


    public ServiceManager(MusicService musicService) throws RemoteException {
        this.musicService = musicService;
        mMediaNotificationManager = new MediaNotificationManager(musicService);
    }

    public void moveServiceToStartedState(PlaybackStateCompat state,
                                          MediaDescriptionCompat mediaDescriptionCompat) {
        Notification notification =
                mMediaNotificationManager.getNotification(
                        mediaDescriptionCompat, state,
                        mMediaNotificationManager.getmService().getSessionToken());

        if (!musicService.getInStartedState()) {
            Context context = mMediaNotificationManager.getmService();
            ContextCompat.startForegroundService(context, new Intent(context, MusicService.class));
            musicService.setInStartedState(true);
        }

        musicService.startForeground(MediaNotificationManager.NOTIFICATION_ID, notification);
    }

    public void updateNotificationForPause(PlaybackStateCompat state,
                                           MediaDescriptionCompat mediaDescriptionCompat) {
        LogHelper.d(TAG, "updateNotificationForPause");
        musicService.stopForeground(false);
        Notification notification =
                mMediaNotificationManager.getNotification(
                        mediaDescriptionCompat, state,
                        musicService.getSessionToken());
        mMediaNotificationManager.getmNotificationManager()
                .notify(MediaNotificationManager.NOTIFICATION_ID, notification);
    }

    public void moveServiceOutOfStartedState() {
        LogHelper.d(TAG, "moveServiceOutOfStartedState");
        musicService.stopForeground(true);
        musicService.stopSelf();
        musicService.setInStartedState(false);
        mMediaNotificationManager.unregisterReceiver();
    }
}
