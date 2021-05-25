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

//        @Deprecated ("redundant after FavouriteMusicDAO class added")
//        @InternalCoroutinesApi
//        @JvmStatic
//        @ExperimentalCoroutinesApi
//        fun loadChildrenMediaItemsForFavourites(mediaBrowser: MediaBrowserCompat,
//                                                callback: (Result<List<MediaBrowserCompat.MediaItem>>?) -> Unit) {
//
//            CoroutineScope(Job() + Dispatchers.Main).launch { //mediaBrowser is not thread-safe and should be used from thread when it was constructed
//                initMediaBrowserLoader(MEDIA_ID_FAVOURITES, mediaBrowser).onEach { value ->
//                    run {
//                        when (value) {
//                            is Result.Success<List<MediaBrowserCompat.MediaItem>> -> {
//                                //next code do not call callback from 'My favourites' if no favourites media
//                                tryToGetFavouriteCategoryMediaId(value.data)?.let {
//                                    initMediaBrowserLoader(it, mediaBrowser)
//                                            .onEach { favourites ->
//                                                run {
//                                                    if (favourites is Result.Success<List<MediaBrowserCompat.MediaItem>>) {
//                                                        if (favourites.data.isNotEmpty()) {
//                                                            callback(value)
//                                                        }
//                                                    }
//                                                }
//                                            }.catch { e -> LogHelper.e(TAG, e) }
//                                            .collect()
//                                }
//                            }
//                            is Result.Error -> {
//                                callback(value)
//                            }
//                            else -> {
//                                callback(Result.Error(Exception("Unexpected error")))
//                            }
//                        }
//                    }
//                }
//                        .catch { e -> LogHelper.e(TAG, e) }
//                        .collect()
//            }
//
//        }

//        private fun tryToGetFavouriteCategoryMediaId(mediaItems: List<MediaBrowserCompat.MediaItem>): String? {
//            return if (mediaItems.isNotEmpty()
//                    && mediaItems.size == 1
//                    && mediaItems[0].description.mediaId != null
//                    && MediaIDHelper.isBrowseable(mediaItems[0].description.mediaId!!)
//                    && (mediaItems[0].description.mediaId?.contains(MEDIA_ID_FAVOURITES) == true)) {
//
//                mediaItems[0].description.mediaId
//            } else {
//                null
//            }
//        }

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

//        @Deprecated("Not tested - may be broken - It's better to use callbackFlow and Channels for multiple callbacks," +
//                " see https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/callback-flow.html",
//                ReplaceWith("initMediaBrowserLoader"))
//        @ExperimentalCoroutinesApi
//        //idea taken here https://medium.com/swlh/coroutines-tips-and-tricks-callbacks-synchronous-way-to-work-with-asynchronous-code-b9fb840fb793
//        private suspend fun initMediaBrowserLoaderDeprecated(
//                rootMediaId: String,
//                mediaBrowser: MediaBrowserCompat):
//                Result<List<MediaBrowserCompat.MediaItem>>? =
//                suspendCancellableCoroutine { cont: CancellableContinuation<Result<List<MediaBrowserCompat.MediaItem>>?> ->
//
//                    mediaBrowser.subscribe(rootMediaId,
//                            object : MediaBrowserCompat.SubscriptionCallback() {
//                                override fun onChildrenLoaded(
//                                        parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
//                                    super.onChildrenLoaded(parentId, children)
//                                    if (children.isEmpty()) {
//                                        LogHelper.e(TAG, "Error on onChildrenLoaded")
//                                        if (cont.isActive) {
//                                            cont.resume(Result.Error(Exception("Empty result")))
//                                        }
//                                    } else {
//                                        LogHelper.d(TAG, "onChildrenLoaded, parentId="
//                                                + parentId + "  count=" + children.size)
//                                        if (cont.isActive) {
//                                            cont.resume(Result.Success(children))
//                                            LogHelper.d(TAG, "resumed")
//                                        }
//                                    }
//                                }
//                            })
//                }
//
//
//        @Deprecated("Not tested - may be broken - It's better to use callbackFlow and Channels for multiple callbacks",
//                ReplaceWith("loadChildrenMediaItems"))
//        @JvmStatic
//        @ExperimentalCoroutinesApi
//        fun loadChildrenMediaItemsDeprecated(
//                mediaBrowser: MediaBrowserCompat,
//                vararg mediaRoots: String,
//                callback: (Result<List<MediaBrowserCompat.MediaItem>>?) -> Unit) {
//            CoroutineScope(Job() + Dispatchers.Main).launch { //mediaBrowser is not thread-safe and should be used from thread when it was constructed
//                for (root in mediaRoots) {
//                    mediaBrowser.unsubscribe(root)
//                    callback(initMediaBrowserLoaderDeprecated(root, mediaBrowser))
//                }
//            }
//        }
    }
}
