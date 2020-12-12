package com.ashomok.lullabies.ui.main_activity

import android.support.v4.media.MediaBrowserCompat
import com.ashomok.lullabies.utils.LogHelper
import com.ashomok.lullabies.utils.Result
import kotlinx.coroutines.*
import kotlin.coroutines.resume

class MediaBrowserLoader {
    companion object {
        val TAG = LogHelper.makeLogTag(MusicPlayerPresenter::class.java)

        @JvmStatic
        @ExperimentalCoroutinesApi
        fun loadChildrenMediaItems(mediaBrowser: MediaBrowserCompat,
                                   vararg mediaRoots: String,
                                   callback: (Result<List<MediaBrowserCompat.MediaItem>>?) -> Unit) {
            CoroutineScope(Job() + Dispatchers.Main).launch { //mediaBrowser is not thread-safe and should be used from thread when it was constructed
                for (root in mediaRoots) {
                    mediaBrowser.unsubscribe(root)

                    callback(initMediaBrowserLoader(root, mediaBrowser))
                }
            }
        }

        //idea taken here https://medium.com/swlh/coroutines-tips-and-tricks-callbacks-synchronous-way-to-work-with-asynchronous-code-b9fb840fb793
        @ExperimentalCoroutinesApi
        private suspend fun initMediaBrowserLoader(
                rootMediaId: String,
                mediaBrowser: MediaBrowserCompat):
                Result<List<MediaBrowserCompat.MediaItem>>? {
            return suspendCancellableCoroutine { cont: CancellableContinuation<Result<List<MediaBrowserCompat.MediaItem>>?> ->
                mediaBrowser.subscribe(rootMediaId,
                        object : MediaBrowserCompat.SubscriptionCallback() {
                            override fun onChildrenLoaded(
                                    parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
                                super.onChildrenLoaded(parentId, children)
                                if (children.isEmpty()) {
                                    LogHelper.e(TAG, "Error on onChildrenLoaded")
                                    Result.Error(Exception("Error on onChildrenLoaded"))
                                    cont.resume(Result.Error(Exception("Empty result")))

                                } else {
                                    LogHelper.d(TAG, "onChildrenLoaded, parentId="
                                            + parentId + "  count=" + children.size)
                                    Result.Success(children)
                                    cont.resume(Result.Success(children))
                                }
                            }
                        })
            }
        }
    }
}