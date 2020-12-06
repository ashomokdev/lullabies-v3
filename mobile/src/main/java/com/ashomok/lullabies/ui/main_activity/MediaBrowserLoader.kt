package com.ashomok.lullabies.ui.main_activity

import android.support.v4.media.MediaBrowserCompat
import androidx.annotation.WorkerThread
import com.ashomok.lullabies.utils.LogHelper
import com.ashomok.lullabies.utils.Result
import kotlinx.coroutines.*
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume

class MediaBrowserLoader {
    companion object {
        val TAG = LogHelper.makeLogTag(MusicPlayerPresenter::class.java)

        //idea taken here https://medium.com/swlh/coroutines-tips-and-tricks-callbacks-synchronous-way-to-work-with-asynchronous-code-b9fb840fb793
        @ExperimentalCoroutinesApi
        private suspend fun initMediaBrowserLoader(
                rootMediaId: String,
                mediaBrowser: MediaBrowserCompat):
                Result<List<MediaBrowserCompat.MediaItem>>? {
            return suspendCancellableCoroutine {
                cont: CancellableContinuation<Result<List<MediaBrowserCompat.MediaItem>>?> ->
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

        @JvmStatic
        @ExperimentalCoroutinesApi
        fun loadChildrenMediaItems(rootMediaId: String, mediaBrowser: MediaBrowserCompat,
                                   callback: (Result<List<MediaBrowserCompat.MediaItem>>?) -> Unit) {
            mediaBrowser.unsubscribe(rootMediaId)
            CoroutineScope(Job() + Dispatchers.Main).launch {
                callback(initMediaBrowserLoader(rootMediaId, mediaBrowser))
            }
        }
    }
}