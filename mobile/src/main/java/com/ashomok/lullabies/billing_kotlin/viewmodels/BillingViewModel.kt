/**
 * Copyright (C) 2018 Google Inc. All Rights Reserved.
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
package com.ashomok.lullabies.billing_kotlin.viewmodels

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.ashomok.lullabies.billing_kotlin.BillingRepository
import com.ashomok.lullabies.billing_kotlin.localdb.AdsFreeForever
import com.ashomok.lullabies.billing_kotlin.localdb.AugmentedSkuDetails
import com.ashomok.lullabies.utils.LogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

/**
 * Notice just how small and simple this BillingViewModel is!!
 *
 * This beautiful simplicity is the result of keeping all the hard work buried inside the
 * [BillingRepository] and only inside the [BillingRepository]. The rest of your app
 * is now free from [BillingClient] tentacles!! And this [BillingViewModel] is the one and only
 * object the rest of your Android team need to know about billing.
 *
 */
class BillingViewModel(application: Application) : AndroidViewModel(application) {
    val adsFreeForeverLiveData: LiveData<AdsFreeForever>
    val inappSkuDetailsListLiveData: LiveData<List<AugmentedSkuDetails>>

    private val TAG = LogHelper.makeLogTag(BillingViewModel::class.java)

    private val viewModelScope = CoroutineScope(Job() + Dispatchers.Main)
    private val repository: BillingRepository

    init {
        repository = BillingRepository.getInstance(application)
        repository.startDataSourceConnections()
        adsFreeForeverLiveData = repository.adsFreeForeverLiveData
        inappSkuDetailsListLiveData = repository.inappSkuDetailsListLiveData
    }

    /**
     * Not used in this sample app. But you may want to force refresh in your own app (e.g.
     * pull-to-refresh)
     */
    fun queryPurchases() = repository.queryPurchasesAsync()

    override fun onCleared() {
        super.onCleared()
        LogHelper.d(TAG, "onCleared")
        repository.endDataSourceConnections()
        viewModelScope.coroutineContext.cancel()
    }

    fun makePurchase(activity: Activity, augmentedSkuDetails: AugmentedSkuDetails) {
        repository.launchBillingFlow(activity, augmentedSkuDetails)
    }

}