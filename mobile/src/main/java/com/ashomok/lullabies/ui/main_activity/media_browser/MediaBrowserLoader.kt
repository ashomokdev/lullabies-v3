package com.ashomok.lullabies.ui.main_activity.media_browser

import android.support.v4.media.MediaBrowserCompat
import com.ashomok.lullabies.utils.LogHelper
import com.ashomok.lullabies.utils.MediaIDHelper
import com.ashomok.lullabies.utils.MediaIDHelper.MEDIA_ID_FAVOURITES
import com.ashomok.lullabies.utils.Result
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*
import kotlin.coroutines.resume

class MediaBrowserLoader {
    companion object {
        val TAG = LogHelper.makeLogTag(MediaBrowserLoader::class.java)

        @InternalCoroutinesApi
        @JvmStatic
        @ExperimentalCoroutinesApi
        fun loadChildrenMediaItems(mediaBrowser: MediaBrowserCompat,
                                   mediaRoot: String,
                                   callback: (Result<List<MediaBrowserCompat.MediaItem>>?) -> Unit) {

            CoroutineScope(Job() + Dispatchers.Main).launch { //mediaBrowser is not thread-safe and should be used from thread when it was constructed
                initMediaBrowserLoader(mediaRoot, mediaBrowser).onEach { value ->
                    run {
                        when (value) {
                            is Result.Success<List<MediaBrowserCompat.MediaItem>> -> {
                                callback(value)
                            }
                            is Result.Error -> {
                                callback(value)
                            }
                            else -> {
                                callback(Result.Error(Exception("Unexpected error")))
                            }
                        }
                    }
                }
                        .catch { e -> LogHelper.e(TAG, e) }
                        .collect()
            }

        }

        @ExperimentalCoroutinesApi
        private suspend fun initMediaBrowserLoader(
                rootMediaId: String,
                mediaBrowser: MediaBrowserCompat):
                Flow<Result<List<MediaBrowserCompat.MediaItem>>?> =

                callbackFlow {
                    LogHelper.d(TAG, "initMediaBrowserLoader called with $rootMediaId")
                    mediaBrowser.unsubscribe(rootMediaId)

                    val callback: MediaBrowserCompat.SubscriptionCallback =
                            object : MediaBrowserCompat.SubscriptionCallback() {
                                override fun onChildrenLoaded(
                                        parentId: String,
                                        children: MutableList<MediaBrowserCompat.MediaItem>) {
                                    super.onChildrenLoaded(parentId, children)
                                    LogHelper.d(TAG, "onChildrenLoaded, parentId="
                                            + parentId + "  count=" + children.size)
                                    sendBlocking(Result.Success(children))
                                }
                            }

                    mediaBrowser.subscribe(rootMediaId, callback)

                    /*
                     * Suspends until either 'onCompleted'/'onApiError' from the callback is invoked
                     * or flow collector is cancelled (e.g. by 'take(1)' or because a collector's coroutine was cancelled).
                     * In both cases, callback will be properly unregistered.
                     */
                    awaitClose { mediaBrowser.unsubscribe(rootMediaId) }
                }
    }
}
