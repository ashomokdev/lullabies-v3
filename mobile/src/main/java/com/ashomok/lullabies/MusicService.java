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

 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.media.MediaMetadata;
 import android.media.MediaPlayer;
 import android.media.session.MediaSession;
 import android.media.session.PlaybackState;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.RemoteException;
 import android.service.media.MediaBrowserService;
 import android.support.v4.media.MediaBrowserCompat.MediaItem;
 import android.support.v4.media.MediaDescriptionCompat;
 import android.support.v4.media.MediaMetadataCompat;
 import android.support.v4.media.session.MediaSessionCompat;
 import android.support.v4.media.session.PlaybackStateCompat;

 import androidx.annotation.NonNull;
 import androidx.media.MediaBrowserServiceCompat;
 import androidx.media.session.MediaButtonReceiver;
 import androidx.mediarouter.media.MediaRouter;

 import com.ashomok.lullabies.model.MusicProvider;
 import com.ashomok.lullabies.playback.CastPlayback;
 import com.ashomok.lullabies.playback.LocalPlayback;
 import com.ashomok.lullabies.playback.Playback;
 import com.ashomok.lullabies.playback.PlaybackManager;
 import com.ashomok.lullabies.playback.QueueManager;
 import com.ashomok.lullabies.ui.NowPlayingActivity;
 import com.ashomok.lullabies.utils.LogHelper;
 import com.ashomok.lullabies.utils.TvHelper;
 import com.google.android.gms.cast.framework.CastContext;
 import com.google.android.gms.cast.framework.CastSession;
 import com.google.android.gms.cast.framework.SessionManager;
 import com.google.android.gms.cast.framework.SessionManagerListener;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GoogleApiAvailability;

 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.List;

 import static com.ashomok.lullabies.utils.MediaIDHelper.MEDIA_ID_EMPTY_ROOT;
 import static com.ashomok.lullabies.utils.MediaIDHelper.MEDIA_ID_ROOT;

 /**
  * This class provides a MediaBrowser through a service. It exposes the media library to a browsing
  * client, through the onGetRoot and onLoadChildren methods. It also creates a MediaSession and
  * exposes it through its MediaSession.Token, which allows the client to create a MediaController
  * that connects to and send control commands to the MediaSession remotely. This is useful for
  * user interfaces that need to interact with your media session, like Android Auto. You can
  * (should) also use the same service from your app's UI, which gives a seamless playback
  * experience to the user.
  * <p>
  * To implement a MediaBrowserService, you need to:
  *
  * <ul>
  *
  * <li> Extend {@link MediaBrowserService}, implementing the media browsing
  * related methods {@link MediaBrowserService#onGetRoot} and
  * {@link MediaBrowserService#onLoadChildren};
  * <li> In onCreate, start a new {@link MediaSession} and notify its parent
  * with the session's token {@link MediaBrowserService#setSessionToken};
  *
  * <li> Set a callback on the
  * {@link MediaSession#setCallback(MediaSession.Callback)}.
  * The callback will receive all the user's actions, like play, pause, etc;
  *
  * <li> Handle all the actual music playing using any method your app prefers (for example,
  * {@link MediaPlayer})
  *
  * <li> Update playbackState, "now playing" metadata and queue, using MediaSession proper methods
  * {@link MediaSession#setPlaybackState(PlaybackState)}
  * {@link MediaSession#setMetadata(MediaMetadata)} and
  *
  * <li> Declare and export the service in AndroidManifest with an intent receiver for the action
  * android.media.browse.MediaBrowserService
  *
  * </ul>
  * <p>
  * To make your app compatible with Android Auto, you also need to:
  *
  * <ul>
  *
  * <li> Declare a meta-data tag in AndroidManifest.xml linking to a xml resource
  * with a &lt;automotiveApp&gt; root element. For a media app, this must include
  * an &lt;uses name="media"/&gt; element as a child.
  * For example, in AndroidManifest.xml:
  * &lt;meta-data android:name="com.google.android.gms.car.application"
  * android:resource="@xml/automotive_app_desc"/&gt;
  * And in res/values/automotive_app_desc.xml:
  * &lt;automotiveApp&gt;
  * &lt;uses name="media"/&gt;
  * &lt;/automotiveApp&gt;
  *
  * </ul>
  *
  * @see <a href="README.md">README.md</a> for more details.
  */
 public class MusicService extends MediaBrowserServiceCompat implements
         PlaybackManager.PlaybackServiceCallback {

     private static final String TAG = LogHelper.makeLogTag(MusicService.class);

     // Extra on MediaSession that contains the Cast device name currently connected to
     public static final String EXTRA_CONNECTED_CAST = "com.ashomok.lullabies.CAST_NAME";
     // The action of the incoming Intent indicating that it contains a command
     // to be executed (see {@link #onStartCommand})
     public static final String ACTION_CMD = "com.ashomok.lullabies.ACTION_CMD";
     // The key in the extras of the incoming Intent indicating the command that
     // should be executed (see {@link #onStartCommand})
     public static final String CMD_NAME = "CMD_NAME";
     // A value of a CMD_NAME key in the extras of the incoming Intent that
     // indicates that the music playback should be paused (see {@link #onStartCommand})
     public static final String CMD_PAUSE = "CMD_PAUSE";
     // A value of a CMD_NAME key in the extras of the incoming Intent that
     // indicates that the music playback should be stopped (see {@link #onStartCommand})
     public static final String CMD_STOP = "CMD_STOP";

     // A value of a CMD_NAME key that indicates that the music playback should switch
     // to local playback from cast playback.
     public static final String CMD_STOP_CASTING = "CMD_STOP_CASTING"; //todo i,plement

     // A value of a CMD_NAME key that indicates that the music service should be stopped.
     public static final String CMD_STOP_SERVICE = "CMD_STOP_SERVICE"; //todo i,plement

     // Delay stopSelf by using a handler.
     private static final int STOP_DELAY = 30000;

     private MusicProvider mMusicProvider;
     private PlaybackManager mPlaybackManager;

     private MediaSessionCompat mSession;
     private ServiceManager serviceManager;
     private Bundle mSessionExtras;
     private final DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);
     private MediaRouter mMediaRouter;
     private PackageValidator mPackageValidator;
     private SessionManager mCastSessionManager;
     private SessionManagerListener<CastSession> mCastSessionManagerListener;

     private boolean inStartedState; //todo remove & simplify


     /*
      * (non-Javadoc)
      * @see android.app.Service#onCreate()
      */
     @Override
     public void onCreate() {
         super.onCreate();
         LogHelper.d(TAG, "onCreate");

         mMusicProvider = new MusicProvider();

         // To make the app more responsive, fetch and cache catalog information now.
         // This can help improve the response time in the method
         // {@link #onLoadChildren(String, Result<List<MediaItem>>) onLoadChildren()}.
         mMusicProvider.retrieveMediaAsync(null /* Callback */);

         mPackageValidator = new PackageValidator(this);

         QueueManager queueManager = new QueueManager(mMusicProvider, getResources(),
                 new QueueManager.MetadataUpdateListener() {
                     @Override
                     public void onMetadataChanged(MediaMetadataCompat metadata) {
                         mSession.setMetadata(metadata);
                     }

                     @Override
                     public void onMetadataRetrieveError() {
                         mPlaybackManager.updatePlaybackState(
                                 getString(R.string.error_no_metadata));
                     }

                     @Override
                     public void onCurrentQueueIndexUpdated(int queueIndex) {
                         mPlaybackManager.handlePlayRequest();
                     }

                     @Override
                     public void onQueueUpdated(String title,
                                                List<MediaSessionCompat.QueueItem> newQueue) {
                         mSession.setQueue(newQueue);
                         mSession.setQueueTitle(title);
                     }
                 });

         LocalPlayback playback = new LocalPlayback(this, mMusicProvider);
         mPlaybackManager = new PlaybackManager(this, getResources(), mMusicProvider, queueManager,
                 playback);

         // Start a new MediaSession
         mSession = new MediaSessionCompat(this, "MusicService");
         setSessionToken(mSession.getSessionToken());
         mSession.setCallback(mPlaybackManager.getMediaSessionCallback());
         mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                 MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

         Context context = getApplicationContext();
         Intent intent = new Intent(context, NowPlayingActivity.class);
         PendingIntent pi = PendingIntent.getActivity(context, 99 /*request code*/,
                 intent, PendingIntent.FLAG_UPDATE_CURRENT);
         mSession.setSessionActivity(pi);

         mSessionExtras = new Bundle();

         mSession.setExtras(mSessionExtras);

         mPlaybackManager.updatePlaybackState(null);

         try {
             serviceManager = new ServiceManager(this);
         } catch (RemoteException e) {
             throw new IllegalStateException("Could not create a ServiceManager", e);
         }

         int playServicesAvailable =
                 GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

         if (!TvHelper.isTvUiMode(this) && playServicesAvailable == ConnectionResult.SUCCESS) {
             mCastSessionManager = CastContext.getSharedInstance(this).getSessionManager();
             mCastSessionManagerListener = new CastSessionManagerListener();
             mCastSessionManager.addSessionManagerListener(mCastSessionManagerListener,
                     CastSession.class);
         }

         mMediaRouter = MediaRouter.getInstance(getApplicationContext());
     }

     //todo test solution https://stackoverflow.com/a/50888586/3627736 two starts
     //https://github.com/Truiton/ForegroundService/blob/master/app/src/main/java/com/truiton/foregroundservice/ForegroundService.java

     /**
      * (non-Javadoc)
      *
      * @see Service#onStartCommand(Intent, int, int)
      */
     @Override
     public int onStartCommand(Intent startIntent, int flags, int startId) {


         int result;
         if (startIntent == null) {
             result = START_STICKY_COMPATIBILITY;
         } else {
             String action = startIntent.getAction();
             String command = startIntent.getStringExtra(CMD_NAME);
             if (ACTION_CMD.equals(action)) {
                 if (CMD_PAUSE.equals(command)) {
                     mPlaybackManager.handlePauseRequest();
                 } else if (CMD_STOP.equals(command)) {
                     mPlaybackManager.handleStopRequest(null);
                 } else if (CMD_STOP_CASTING.equals(command)) {
                     CastContext.getSharedInstance(this).getSessionManager().endCurrentSession(true);
                 } else if (CMD_STOP_SERVICE.equals(command)) {
                     stopForeground(true);
                     stopSelf();
                 }
             } else {
                 // Try to handle the intent as a media button event wrapped by MediaButtonReceiver
                 MediaButtonReceiver.handleIntent(mSession, startIntent);
             }
             result = START_STICKY;
         }
         // Reset the delay handler to enqueue a message to stop the service if
         // nothing is playing.
         mDelayedStopHandler.removeCallbacksAndMessages(null);
         mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
         return result;
     }

     /**
      * old version from 24 apr 2020
      *
      * @param rootIntent
      */
//     /**
//      * (non-Javadoc)
//      *
//      * @see Service#onStartCommand(Intent, int, int)
//      */
//     @Override
//     public int onStartCommand(Intent startIntent, int flags, int startId) {
//
//         if (startIntent != null) {
//             String action = startIntent.getAction();
//             String command = startIntent.getStringExtra(CMD_NAME);
//             if (ACTION_CMD.equals(action)) {
//                 if (CMD_PAUSE.equals(command)) {
//                     mPlaybackManager.handlePauseRequest();
//                 } else if (CMD_STOP.equals(command)) {
//                     mPlaybackManager.handleStopRequest(null);
//                 }
//             } else {
//                 // Try to handle the intent as a media button event wrapped by MediaButtonReceiver
//                 MediaButtonReceiver.handleIntent(mSession, startIntent);
//             }
//         }
//         // Reset the delay handler to enqueue a message to stop the service if
//         // nothing is playing.
//         mDelayedStopHandler.removeCallbacksAndMessages(null);
//         mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
//         return START_STICKY;
//     }

     /*
      * Handle case when user swipes the app away from the recents apps list by
      * stopping the service (and any ongoing playback).
      */
     @Override
     public void onTaskRemoved(Intent rootIntent) {
         super.onTaskRemoved(rootIntent);
         stopForeground(true); //added
         stopSelf();
     }

     /**
      * (non-Javadoc)
      *
      * @see Service#onDestroy()
      */
     @Override
     public void onDestroy() {
         LogHelper.d(TAG, "onDestroy");
         // Service is being killed, so make sure we release our resources
         mPlaybackManager.handleStopRequest(null);
         serviceManager.moveServiceOutOfStartedState();

         if (mCastSessionManager != null) {
             mCastSessionManager.removeSessionManagerListener(mCastSessionManagerListener,
                     CastSession.class);
         }

         mDelayedStopHandler.removeCallbacksAndMessages(null);
         mSession.release();
     }

     @Override
     public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid,
                                  Bundle rootHints) {
         LogHelper.v(TAG, "OnGetRoot: clientPackageName=" + clientPackageName,
                 "; clientUid=" + clientUid + " ; rootHints=", rootHints);
         // To ensure you are not allowing any arbitrary app to browse your app's contents, you
         // need to check the origin:
         if (!mPackageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
             // If the request comes from an untrusted package, return an empty browser root.
             // If you return null, then the media browser will not be able to connect and
             // no further calls will be made to other media browsing methods.
             LogHelper.i(TAG, "OnGetRoot: Browsing NOT ALLOWED for unknown caller. "
                     + "Returning empty browser root so all apps can use MediaController."
                     + clientPackageName);
             return new BrowserRoot(MEDIA_ID_EMPTY_ROOT, null);
         }

         return new BrowserRoot(MEDIA_ID_ROOT, null);
     }

     @Override
     public void onLoadChildren(@NonNull final String parentMediaId,
                                @NonNull final Result<List<MediaItem>> result) {
         LogHelper.d(TAG, "OnLoadChildren: parentMediaId=", parentMediaId);
         if (MEDIA_ID_EMPTY_ROOT.equals(parentMediaId)) {
             result.sendResult(new ArrayList<>());
         } else if (mMusicProvider.isInitialized()) {
             // if music library is ready, return immediately
             result.sendResult(mMusicProvider.getChildren(parentMediaId, getResources()));
         } else {
             // otherwise, only return results when the music library is retrieved
             result.detach();
             mMusicProvider.retrieveMediaAsync(new MusicProvider.Callback() {
                 @Override
                 public void onMusicCatalogReady(boolean success) {
                     result.sendResult(mMusicProvider.getChildren(parentMediaId, getResources()));
                 }
             });
         }
     }

     /**
      * Callback method called from PlaybackManager whenever the music is about to play.
      */
     @Override
     public void onPlaybackStart() {
         LogHelper.d(TAG, "onPlaybackStart");
         mSession.setActive(true);

         mDelayedStopHandler.removeCallbacksAndMessages(null);

         // The service needs to continue running even after the bound client (usually a
         // MediaController) disconnects, otherwise the music playback will stop.
         // Calling startService(Intent) will keep the service running until it is explicitly killed.
     }

     /**
      * Callback method called from PlaybackManager whenever the music stops playing.
      */
     @Override
     public void onPlaybackStop() {
         mSession.setActive(false);
         // Reset the delayed stop handler, so after STOP_DELAY it will be executed again,
         // potentially stopping the service.
         mDelayedStopHandler.removeCallbacksAndMessages(null);
         mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
     }

     @Override
     public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
         mSession.setPlaybackState(newState);
     }

     @Override
     public void updateServiceState(PlaybackStateCompat state, MediaDescriptionCompat description) {

         switch (state.getState()) {
             case PlaybackStateCompat.STATE_PLAYING:
                 serviceManager.moveServiceToStartedState(state, description);
                 break;
             case PlaybackStateCompat.STATE_PAUSED:
                 serviceManager.updateNotificationForPause(state, description);
                 break;
             case PlaybackStateCompat.STATE_STOPPED:
                 serviceManager.moveServiceOutOfStartedState();
                 break;
             default:
                 LogHelper.d(TAG, "state is " + state + " notification not required");
                 break;
         }
     }

    public void setInStartedState(boolean inStartedState) {
         this.inStartedState = inStartedState;
     }

     public boolean getInStartedState() {
         return inStartedState;
     }

     /**
      * A simple handler that stops the service if playback is not active (playing)
      */
     private static class DelayedStopHandler extends Handler {
         private final WeakReference<MusicService> mWeakReference;

         private DelayedStopHandler(MusicService service) {
             mWeakReference = new WeakReference<>(service);
         }

         @Override
         public void handleMessage(Message msg) {
             MusicService service = mWeakReference.get();
             if (service != null && service.mPlaybackManager.getPlayback() != null) {
                 if (service.mPlaybackManager.getPlayback().isPlaying()) {
                     LogHelper.d(TAG, "Ignoring delayed stop since the media player is in use.");
                 } else {
                     LogHelper.d(TAG, "Stopping service with delay handler.");
                     service.stopForeground(true); //added
                     service.stopSelf();
                 }
             }
         }
     }

     /**
      * Session Manager Listener responsible for switching the Playback instances
      * depending on whether it is connected to a remote player.
      */
     private class CastSessionManagerListener implements SessionManagerListener<CastSession> {

         @Override
         public void onSessionEnded(CastSession session, int error) {
             LogHelper.d(TAG, "onSessionEnded");
             mSessionExtras.remove(EXTRA_CONNECTED_CAST);
             mSession.setExtras(mSessionExtras);
             Playback playback = new LocalPlayback(MusicService.this, mMusicProvider);
             mMediaRouter.setMediaSessionCompat(null);
             mPlaybackManager.switchToPlayback(playback, false);
         }

         @Override
         public void onSessionResumed(CastSession session, boolean wasSuspended) {
         }

         @Override
         public void onSessionStarted(CastSession session, String sessionId) {
             // In case we are casting, send the device name as an extra on MediaSession metadata.
             mSessionExtras.putString(EXTRA_CONNECTED_CAST,
                     session.getCastDevice().getFriendlyName());
             mSession.setExtras(mSessionExtras);
             // Now we can switch to CastPlayback
             Playback playback = new CastPlayback(mMusicProvider, MusicService.this);
             mMediaRouter.setMediaSessionCompat(mSession);
             mPlaybackManager.switchToPlayback(playback, true);
         }

         @Override
         public void onSessionStarting(CastSession session) {
         }

         @Override
         public void onSessionStartFailed(CastSession session, int error) {
         }

         @Override
         public void onSessionEnding(CastSession session) {
             // This is our final chance to update the underlying stream position
             // In onSessionEnded(), the underlying CastPlayback#mRemoteMediaClient
             // is disconnected and hence we update our local value of stream position
             // to the latest position.
             mPlaybackManager.getPlayback().updateLastKnownStreamPosition();
         }

         @Override
         public void onSessionResuming(CastSession session, String sessionId) {
         }

         @Override
         public void onSessionResumeFailed(CastSession session, int error) {
         }

         @Override
         public void onSessionSuspended(CastSession session, int reason) {
         }
     }
 }
