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
package com.ashomok.lullabies.billing_kotlin

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.*
import com.ashomok.lullabies.billing_kotlin.BillingRepository.AppSku.INAPP_SKUS
import com.ashomok.lullabies.billing_kotlin.Security.BASE_64_ENCODED_PUBLIC_KEY
import com.ashomok.lullabies.billing_kotlin.localdb.AdsFreeForever
import com.ashomok.lullabies.billing_kotlin.localdb.AugmentedSkuDetails
import com.ashomok.lullabies.billing_kotlin.localdb.Entitlement
import com.ashomok.lullabies.billing_kotlin.localdb.LocalBillingDb
import kotlinx.coroutines.*
import java.util.*

/**
 *
 * FOREWORD:
 *
 * To avoid ambiguity and for simplified readability:
 *
 *  - the term "user" shall refer to people who download the app and buy
 *    products or subscriptions.
 *
 *  - the term "client" shall refer to those developers/engineers on the team who depend on the
 *    billing repository as an API.
 *
 * GOVERNING ARCHITECTURE:
 *
 * A Google Play Billing implementation depends on purchase confirmation data from three different
 * data sources: the Google Play Billing service, a secure server, and the app's own local cache.
 * A repository module is needed to manage these different data sources and associated operations
 * so that the rest of the app can access this data. Let this module be called [BillingRepository].
 * And this module shall handle all interactions with the
 * _Play Billing Library_, the billing portions of a secure server, and the billing portions of
 * the app’s local cache.
 *
 * What the rest of the app gets, then, is an API in the form of a ViewModel that comprises:
 * a list of inventory (i.e. what’s for sale); a purchase function (i.e. how to buy); and a set
 * of entitlements (i.e. what the user owns). This recommended structure removes some of the
 * complexities of Google Play Billing. Managed products, consumable products, subscriptions, and
 * other product types are handled identically through a unified API.
 *
 * What this also means is that all of the work is now concentrated in one module, the
 * [BillingRepository]. This repository shall track everything the app is selling and how the app
 * is selling them. The app should not be selling anything that's not listed in this
 * repository. Therefore, the engineers responsible for implementing the [BillingRepository]
 * need to fully understand the app's business model.
 *
 * Figure 1 shows the app from the perspective of the billing module.
 *
 * ```
 *    _____________
 *  _|___________  |     _____________       _____________     __________________________
 * |             | |    |             |     |             |  /|Secure Server Billing Data|
 * |  Activity   | |    | ViewModel   |     | Billing     | /  --------------------------
 * |     &       | |    |    Set      |     | Repository  |/   ________________________
 * |  Fragment   | |--->|             |---->|             |---|Local Cache Billing Data|
 * |    Set      | |    |             |     |             |    ------------------------
 * |             | |    |             |     |             |    _________________
 * |             | |    |             |     |             |---|Play Billing Data|
 * |             |_|    |             |     |             |    -----------------
 * |_____________|      |_____________|     |_____________|
 *
 *  Figure 1
 * ```
 *
 * DATA DYNAMICS:
 *
 * It’s important to consider data mechanics when designing a feature as crucial as the
 * app’s billing repository. Where the billing data rests and how it flows through the repository
 * are central aspects of the design.
 *
 * The Google Play Billing Library 2.0 and above (PBL 2.0+) enables us to draw sharp distinction
 * amongst three primary implementations of billing:
 *
 * 1- Billing integration with no offline access to entitlements
 *
 * 2- Server-reliant billing integration with offline access to some entitlements
 *
 * 3- Serverless billing integration
 *
 * While these three primary integrations do have possible combined derivatives, in the coming
 * weeks we will provide samples or codelabs that address each of these primary integrations.
 *
 * The following three figures depict each model:
 *
 *
 * ```
 * Figure 2 -- Billing integration with no offline access to entitlements
 *
 *  _____                        _________________
 * |Start|----------------------|launchBillingFlow|
 *  -----                        -----------------
 *                                        |
 *                                  ______v____________
 *                                 |onPurchasesUpdated |
 *                                  -------------------
 *                                 /      |
 *                   ITEM_ALREADY_OWNED   |
 *                               /        |
 *  _____       ________________v__       |
 * |Start|-----|queryPurchasesAsync|      OK
 *  -----       -------------------       |
 *                               \        |
 *                               v________v_______
 *                              |processPurchases |
 *                               -----------------
 *                                        |
 *                                        |
 *                                 _______v__________
 *                                | ? isConsumable?  |
 *                                 ------------------
 *                                      |
 *                                      |
 *                                     / \
 *                                 yes/   \
 *                ___________________v_    \
 *               |[processConsumables]|     \
 *                --------------------       \
 *                             |              \
 *                             |               \
 *                             v_______________v________
 *                            |     REMOTE SERVER:      |
 *                            | - verify purchases      |
 *                            | - disburse entitlements |
 *                             -------------------------
 *                                        |
 *                                  ______v____
 *                                 |  LiveData |
 *                                  -----------
 *                                        |
 *                         _______________v_____________________
 *                        |  API / Client Interface / ViewModel |
 *                        ---------------------------------------
 *
 * ```
 *
 *
 * ```
 * Figure 3 -- Server-reliant billing integration with offline access to some entitlements
 *
 *  _____                        _________________
 * |Start|----------------------|launchBillingFlow|
 *  -----                        -----------------
 *                                        |
 *                                  ______v____________
 *                                 |onPurchasesUpdated |
 *                                  -------------------
 *                                 /      |
 *                   ITEM_ALREADY_OWNED   |
 *                               /        |
 *  _____       ________________v__       |
 * |Start|-----|queryPurchasesAsync|      OK
 *  -----       -------------------       |
 *                               \        |
 *                               v________v_______
 *                              |processPurchases |
 *                               -----------------
 *                                        |
 *                                        |
 *                                 _______v__________
 *                                | ? isConsumable?  |
 *                                 ------------------
 *                                      |
 *                                      |
 *                                     / \
 *                                 yes/   \
 *                ___________________v_    \
 *               |[processConsumables]|     \
 *                --------------------       \
 *                             |              \
 *                             |               \
 *                             v_______________v________
 *                            |     REMOTE SERVER:      |
 *                            | - verify purchases      |
 *                            | - disburse entitlements |
 *                             -------------------------
 *                                        |
 *                                        |
 *                                 _______v________
 *                                | local database |
 *                                 ----------------
 *                                        |
 *                                  ______v____
 *                                 |  LiveData |
 *                                  -----------
 *                                        |
 *                         _______________v_____________________
 *                        |  API / Client Interface / ViewModel |
 *                        ---------------------------------------
 *
 * ```
 *
 *
 * ```
 * Figure 4 -- Serverless billing integration
 *
 *  _____                        _________________
 * |Start|----------------------|launchBillingFlow|
 *  -----                        -----------------
 *                                        |
 *                                  ______v____________
 *                                 |onPurchasesUpdated |
 *                                  -------------------
 *                                 /      |
 *                   ITEM_ALREADY_OWNED   |
 *                               /        |
 *  _____       ________________v__       |
 * |Start|-----|queryPurchasesAsync|      OK
 *  -----       -------------------       |
 *                               \        |
 *                               v________v_______
 *                              |processPurchases |
 *                               -----------------
 *                                        |
 *                                        |
 *                                 _______v__________
 *                                | ? isConsumable?  |
 *                                 ------------------
 *                                 |           |
 *                                 |           |
 *                                 |        ___v___________________
 *                                 |       |  acknowledge purchase |
 *                              yes|        -----------------------
 *                _________________v__         |
 *               | processConsumables |        |
 *                --------------------         |
 *                             |               |
 *                             |               |
 *                             v_______________v________
 *                            |  disburse entitlements |
 *                             -------------------------
 *                                        |
 *                                        |
 *                                 _______v________
 *                                | local database |
 *                                 ----------------
 *                                        |
 *                                  ______v____
 *                                 |  LiveData |
 *                                  -----------
 *                                        |
 *                         _______________v_____________________
 *                        |  API / Client Interface / ViewModel |
 *                        ---------------------------------------
 *
 *
 * ```
 *
 * Figures 2 and 3 process the purchases and disburse the entitlements on the remote server. But
 * Figure 4, being serverless, processes everything inside the Android app. This present sample
 * will follow the flow depicted in Figure 4.
 *
 * Here is a bit more detail on the flow depicted in Figure 4:
 *
 * 1. [launchBillingFlow] and [queryPurchasesAsync] can be called directly from the client:
 *   [launchBillingFlow] may be triggered by a button click when the user wants to buy something;
 *   [queryPurchasesAsync] may be triggered when the app launches, by a pull-to-refresh or an
 *   [Activity] lifecycle event. Hence, they are the starting points in the process.
 *
 * 2. [onPurchasesUpdated] is the callback that the Play [BillingClient] calls in response to its
 *    [launcBillingFlow][BillingClient.launchBillingFlow] being called. If the response code is
 *    [BillingResponse.OK], then developers may go straight to [processPurchases]. If, however, the
 *    response code is [BillingResponse.ITEM_ALREADY_OWNED], then developers should call
 *    [queryPurchasesAsync] to verify if other such already-owned items exist that should be
 *    processed.
 *
 * 3. The [queryPurchasesAsync] method grabs all the active purchases of this user and makes them
 *    available to this app instance. Calling Play's [BillingClient] multiple times is relatively
 *    cheap; it involves no network calls since Play stores the data in its own local cache. The
 *    purchase data is then [processed][processPurchases] and converted to
 *    [premium contents][Entitlement].
 *
 * 4. Finally, all data that end up as part of the public interface of the [BillingRepository]
 *    (i.e. in the [BillingViewModel]), and therefore in other portions of the app, come immediately
 *    from the local cache billing client. The local cache is backed by a [Room] database and all
 *    the data visible to the clients is wrapped in [LiveData] so that changes are reflected in
 *    the clients as soon as they happen.
 *
 *
 * ```
 *
 * FINAL THOUGHT
 *
 * We strongly recommend that you implement Figure 3 or Figure 2 in your app. However, the
 * implementation for Figure 4 is provided for apps that do not already have a server component
 * and do not wish to create one just for Google Play Billing.
 *
 * While the architecture presented here and most of the code is highly reusable, this repository
 * is app-specific. For Trivial Drive, for example, it is tailored to sell three items: a premium
 * car, gas for driving, and gold status. Consequently, this repo must handle logic that
 * deals with what it means for a user to own a premium car or buy gas.
 *
 *  @param application the [Application] context
 */
class BillingRepository private constructor(private val application: Application) :
        PurchasesUpdatedListener, BillingClientStateListener {

    /**
     * The [BillingClient] is the most reliable and primary source of truth for all purchases
     * made through the Google Play Store. The Play Store takes security precautions in guarding
     * the data. Also, the data is available offline in most cases, which means the app incurs no
     * network charges for checking for purchases using the [BillingClient]. The offline bit is
     * because the Play Store caches every purchase the user owns, in an
     * [eventually consistent manner](https://developer.android.com/google/play/billing/billing_library_overview#Keep-up-to-date).
     * This is the only billing client an app is actually required to have on Android. The other
     * two (webServerBillingClient and localCacheBillingClient) are optional.
     *
     * ASIDE. Notice that the connection to [playStoreBillingClient] is created using the
     * applicationContext. This means the instance is not [Activity]-specific. And since it's also
     * not expensive, it can remain open for the life of the entire [Application]. So whether it is
     * (re)created for each [Activity] or [Fragment] or is kept open for the life of the application
     * is a matter of choice.
     */
    lateinit private var playStoreBillingClient: BillingClient

    /**
     * A local cache billing client is important in that the Play Store may be temporarily
     * unavailable during updates. In such cases, it may be important that the users
     * continue to get access to premium data that they own. Alternatively, you may choose not to
     * provide offline access to your premium content.
     *
     * Even beyond offline access to premium content, however, a local cache billing client makes
     * certain transactions easier. Without an offline cache billing client, for instance, the app
     * would need both the secure server and the Play Billing client to be available in order to
     * process consumable products.
     *
     * The data that lives here should be refreshed at regular intervals so that it reflects what's
     * in the Google Play Store.
     */
    lateinit private var localCacheBillingClient: LocalBillingDb

    /**
     * This list tells clients what in-app products are available for sale
     */
    val inappSkuDetailsListLiveData: LiveData<List<AugmentedSkuDetails>> by lazy {
        if (::localCacheBillingClient.isInitialized == false) {
            localCacheBillingClient = LocalBillingDb.getInstance(application)
        }
        localCacheBillingClient.skuDetailsDao().getInappSkuDetails()
    }

    // START list of each distinct item user may own (i.e. entitlements)

    /*
     * The clients are interested in two types of information: what's for sale, and what the user
     * owns access to. subsSkuDetailsListLiveData and inappSkuDetailsListLiveData tell the clients
     * what's for sale. gasTankLiveData, adsFreeForeverLiveData, and goldStatusLiveData will tell the
     * client what the user is entitled to. Notice there is nothing billing-specific about these
     * items; they are just properties of the app. So exposing these items to the rest of the app
     * doesn't mean that clients need to understand how billing works.
     *
     * One approach that isn't recommended would be to provide the clients a list of purchases and
      * let them figure it out from there:
     *
     *
     * ```
     *    val purchasesLiveData: LiveData<Set<CachedPurchase>>
     *        by lazy {
     *            queryPurchasesAsync()
     *             return localCacheBillingClient.purchaseDao().getPurchases()//assuming liveData
     *       }
     * ```
     *
     * In doing this, however, the [BillingRepository] API would not be client-friendly. Instead,
     * you should specify each item for the clients.
     *
     * For instance, this sample app sells three different items: gas, a car, and gold status.
     * Hence, the rest of the app wants to know -- at all time and as it happens -- the
     * following: how much gas the user has; what car the user owns; and does the user have gold
     * status. You don't need to expose any additional implementation details. Also you should
     * provide each one of those items as a [LiveData] so that the appropriate UIs get updated
     * automatically.
     */

    /**
     * Tracks whether this user is entitled to a premium car. This call returns data from the app's
     * own local DB; this way if Play and the secure server are unavailable, users still have
     * access to features they purchased.  Normally this would be a good place to update the local
     * cache to make sure it's always up-to-date. However, onBillingSetupFinished already called
     * queryPurchasesAsync for you; so no need.
     */
    val adsFreeForeverLiveData: LiveData<AdsFreeForever> by lazy {
        if (::localCacheBillingClient.isInitialized == false) {
            localCacheBillingClient = LocalBillingDb.getInstance(application)
        }
        localCacheBillingClient.entitlementsDao().getPremiumCar()
    }

    // END list of each distinct item user may own (i.e. entitlements)

    /**
     * Correlated data sources belong inside a repository module so that the rest of
     * the app can have appropriate access to the data it needs. Still, it may be effective to
     * track the opening (and sometimes closing) of data source connections based on lifecycle
     * events. One convenient way of doing that is by calling this
     * [startDataSourceConnections] when the [BillingViewModel] is instantiated and
     * [endDataSourceConnections] inside [ViewModel.onCleared]
     */
    fun startDataSourceConnections() {
        Log.d(LOG_TAG, "startDataSourceConnections")
        instantiateAndConnectToPlayBillingService()
        localCacheBillingClient = LocalBillingDb.getInstance(application)
    }

    fun endDataSourceConnections() {
        playStoreBillingClient.endConnection()
        // normally you don't worry about closing a DB connection unless you have more than
        // one DB open. so no need to call 'localCacheBillingClient.close()'
        Log.d(LOG_TAG, "startDataSourceConnections")
    }

    private fun instantiateAndConnectToPlayBillingService() {
        playStoreBillingClient = BillingClient.newBuilder(application.applicationContext)
                .enablePendingPurchases() // required or app will crash
                .setListener(this).build()
        connectToPlayBillingService()
    }

    private fun connectToPlayBillingService(): Boolean {
        Log.d(LOG_TAG, "connectToPlayBillingService")
        if (!playStoreBillingClient.isReady) {
            playStoreBillingClient.startConnection(this)
            return true
        }
        return false
    }

    /**
     * This is the callback for when connection to the Play [BillingClient] has been successfully
     * established. It might make sense to get [SkuDetails] and [Purchases][Purchase] at this point.
     */
    override fun onBillingSetupFinished(billingResult: BillingResult) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.d(LOG_TAG, "onBillingSetupFinished successfully")
                querySkuDetailsAsync(BillingClient.SkuType.INAPP, INAPP_SKUS)
                queryPurchasesAsync()
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                //Some apps may choose to make decisions based on this knowledge.
                Log.d(LOG_TAG, billingResult.debugMessage)
            }
            else -> {
                //do nothing. Someone else will connect it through retry policy.
                //May choose to send to server though
                Log.d(LOG_TAG, billingResult.debugMessage)
            }
        }
    }

    /**
     * This method is called when the app has inadvertently disconnected from the [BillingClient].
     * An attempt should be made to reconnect using a retry policy. Note the distinction between
     * [endConnection][BillingClient.endConnection] and disconnected:
     * - disconnected means it's okay to try reconnecting.
     * - endConnection means the [playStoreBillingClient] must be re-instantiated and then start
     *   a new connection because a [BillingClient] instance is invalid after endConnection has
     *   been called.
     **/
    override fun onBillingServiceDisconnected() {
        Log.d(LOG_TAG, "onBillingServiceDisconnected")
        connectToPlayBillingService()
    }

    /**
     * BACKGROUND
     *
     * Google Play Billing refers to receipts as [Purchases][Purchase]. So when a user buys
     * something, Play Billing returns a [Purchase] object that the app then uses to release the
     * [Entitlement] to the user. Receipts are pivotal within the [BillingRepositor]; but they are
     * not part of the repo’s public API, because clients don’t need to know about them. When
     * the release of entitlements occurs depends on the type of purchase. For consumable products,
     * the release may be deferred until after consumption by Google Play; for non-consumable
     * products and subscriptions, the release may be deferred until after
     * [BillingClient.acknowledgePurchaseAsync] is called. You should keep receipts in the local
     * cache for augmented security and for making some transactions easier.
     *
     * THIS METHOD
     *
     * [This method][queryPurchasesAsync] grabs all the active purchases of this user and makes them
     * available to this app instance. Whereas this method plays a central role in the billing
     * system, it should be called at key junctures, such as when user the app starts.
     *
     * Because purchase data is vital to the rest of the app, this method is called each time
     * the [BillingViewModel] successfully establishes connection with the Play [BillingClient]:
     * the call comes through [onBillingSetupFinished]. Recall also from Figure 4 that this method
     * gets called from inside [onPurchasesUpdated] in the event that a purchase is "already
     * owned," which can happen if a user buys the item around the same time
     * on a different device.
     */
    fun queryPurchasesAsync() {
        Log.d(LOG_TAG, "queryPurchasesAsync called")
        val purchasesResult = HashSet<Purchase>()
        val result = playStoreBillingClient.queryPurchases(BillingClient.SkuType.INAPP)
        Log.d(LOG_TAG, "queryPurchasesAsync INAPP results: ${result?.purchasesList?.size}")
        result?.purchasesList?.apply { purchasesResult.addAll(this) }

        processPurchases(purchasesResult)
    }

    private fun processPurchases(purchasesResult: Set<Purchase>) =
            CoroutineScope(Job() + Dispatchers.IO).launch {
                Log.d(LOG_TAG, "processPurchases called")
                val validPurchases = HashSet<Purchase>(purchasesResult.size)
                Log.d(LOG_TAG, "processPurchases newBatch content $purchasesResult")
                purchasesResult.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        if (isSignatureValid(purchase)) {
                            validPurchases.add(purchase)
                        }
                    } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                        Log.d(LOG_TAG, "Received a pending purchase of SKU: ${purchase.sku}")
                        // handle pending purchases, e.g. confirm with users about the pending
                        // purchases, prompt them to complete it, etc.
                    }
                }
                val nonConsumables = validPurchases.toList()

                Log.d(LOG_TAG, "processPurchases non-consumables content $nonConsumables")
                /*
                  As is being done in this sample, for extra reliability you may store the
                  receipts/purchases to a your own remote/local database for until after you
                  disburse entitlements. That way if the Google Play Billing library fails at any
                  given point, you can independently verify whether entitlements were accurately
                  disbursed. In this sample, the receipts are then removed upon entitlement
                  disbursement.
                 */
                val testing = localCacheBillingClient.purchaseDao().getPurchases()
                Log.d(LOG_TAG, "processPurchases purchases in the lcl db ${testing.size}")
                localCacheBillingClient.purchaseDao().insert(*validPurchases.toTypedArray())
                acknowledgeNonConsumablePurchasesAsync(nonConsumables)
            }

    /**
     * If you do not acknowledge a purchase, the Google Play Store will provide a refund to the
     * users within a few days of the transaction. Therefore you have to implement
     * [BillingClient.acknowledgePurchaseAsync] inside your app.
     */
    private fun acknowledgeNonConsumablePurchasesAsync(nonConsumables: List<Purchase>) {
        nonConsumables.forEach { purchase ->
            val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase
                    .purchaseToken).build()
            playStoreBillingClient.acknowledgePurchase(params) { billingResult ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        disburseNonConsumableEntitlement(purchase)
                    }
                    else -> Log.d(LOG_TAG, "acknowledgeNonConsumablePurchasesAsync response is ${billingResult.debugMessage}")
                }
            }

        }
    }

    /**
     * This is the final step, where purchases/receipts are converted to premium contents.
     * In this sample, once the entitlement is disbursed the receipt is thrown out.
     */
    private fun disburseNonConsumableEntitlement(purchase: Purchase) =
            CoroutineScope(Job() + Dispatchers.IO).launch {
                when (purchase.sku) {
                    AppSku.ADS_FREE_FOREVER_SKU_ID -> {
                        val adsFreeForever = AdsFreeForever(true)
                        insert(adsFreeForever)
                        localCacheBillingClient.skuDetailsDao()
                                .insertOrUpdate(purchase.sku, adsFreeForever.mayPurchase())
                    }
                }
                localCacheBillingClient.purchaseDao().delete(purchase)
            }

    /**
     * Ideally your implementation will comprise a secure server, rendering this check
     * unnecessary. @see [Security]
     */
    private fun isSignatureValid(purchase: Purchase): Boolean {
        return Security.verifyPurchase(
                BASE_64_ENCODED_PUBLIC_KEY, purchase.originalJson, purchase.signature
        )
    }

    /**
     * Presumably a set of SKUs has been defined on the Google Play Developer Console. This
     * method is for requesting a (improper) subset of those SKUs. Hence, the method accepts a list
     * of product IDs and returns the matching list of SkuDetails.
     *
     * The result is passed to [onSkuDetailsResponse]
     */
    private fun querySkuDetailsAsync(
            @BillingClient.SkuType skuType: String,
            skuList: List<String>) {
        val params = SkuDetailsParams.newBuilder().setSkusList(skuList).setType(skuType).build()
        Log.d(LOG_TAG, "querySkuDetailsAsync for $skuType")
        playStoreBillingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (skuDetailsList.orEmpty().isNotEmpty()) {
                        skuDetailsList.forEach {
                            CoroutineScope(Job() + Dispatchers.IO).launch {
                                localCacheBillingClient.skuDetailsDao().insertOrUpdate(it)
                            }
                        }
                    }
                }
                else -> {
                    Log.e(LOG_TAG, billingResult.debugMessage)
                }
            }
        }
    }

    /**
     * This is the function to call when user wishes to make a purchase. This function will
     * launch the Google Play Billing flow. The response to this call is returned in
     * [onPurchasesUpdated]
     */
    fun launchBillingFlow(activity: Activity, augmentedSkuDetails: AugmentedSkuDetails) =
            launchBillingFlow(activity, SkuDetails(augmentedSkuDetails.originalJson))

    fun launchBillingFlow(activity: Activity, skuDetails: SkuDetails) {
        val purchaseParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build()
        playStoreBillingClient.launchBillingFlow(activity, purchaseParams)
    }

    /**
     * This method is called by the [playStoreBillingClient] when new purchases are detected.
     * The purchase list in this method is not the same as the one in
     * [queryPurchases][BillingClient.queryPurchases]. Whereas queryPurchases returns everything
     * this user owns, [onPurchasesUpdated] only returns the items that were just now purchased or
     * billed.
     *
     * The purchases provided here should be passed along to the secure server for
     * [verification](https://developer.android.com/google/play/billing/billing_library_overview#Verify)
     * and safekeeping. And if this purchase is consumable, it should be consumed, and the secure
     * server should be told of the consumption. All that is accomplished by calling
     * [queryPurchasesAsync].
     */
    override fun onPurchasesUpdated(
            billingResult: BillingResult,
            purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                // will handle server verification, consumables, and updating the local cache
                purchases?.apply { processPurchases(this.toSet()) }
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                // item already owned? call queryPurchasesAsync to verify and process all such items
                Log.d(LOG_TAG, billingResult.debugMessage)
                queryPurchasesAsync()
            }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                connectToPlayBillingService()
            }
            else -> {
                Log.i(LOG_TAG, billingResult.debugMessage)
            }
        }
    }


    @WorkerThread
    suspend private fun insert(entitlement: Entitlement) = withContext(Dispatchers.IO) {
        localCacheBillingClient.entitlementsDao().insert(entitlement)
    }

    companion object {
        private const val LOG_TAG = "BillingRepository"

        @Volatile
        private var INSTANCE: BillingRepository? = null

        fun getInstance(application: Application): BillingRepository =
                INSTANCE ?: synchronized(this) {
                    INSTANCE
                            ?: BillingRepository(application)
                                    .also { INSTANCE = it }
                }
    }

    /**
     * [INAPP_SKUS]
     *
     * If you don't need customization ,then you can define these lists and hardcode them here.
     * That said, there are use cases where you may need customization:
     *
     * - If you don't want to update your APK (or Bundle) each time you change your SKUs, then you
     *   may want to load these lists from your secure server.
     *
     * - If your design is such that users can buy different items from different Activities or
     * Fragments, then you may want to define a list for each of those subsets. I only have two
     * subsets: INAPP_SKUS and SUBS_SKUS
     */

    //todo
    //usign Dagger DI set test values for test purposes
//    example ID android.test.purchased
    // https://developer.android.com/google/play/billing/billing_testing
//    2020-03-14 15:59:56.542 23890-23890/com.ashomok.lullabies W/BillingHelper: Couldn't find purchase lists, trying to find single data.
//    2020-03-14 15:59:56.543 23890-23890/com.ashomok.lullabies W/BillingHelper: Received a bad purchase data.
//    2020-03-14 15:59:56.543 23890-23890/com.ashomok.lullabies W/BillingHelper: Couldn't find single purchase data as well.
    object AppSku {
        val ADS_FREE_FOREVER_SKU_ID = "ads_free_forever"
//        val ADS_FREE_FOREVER_SKU_ID = "android.test.purchased"
        val INAPP_SKUS = listOf(ADS_FREE_FOREVER_SKU_ID)
    }
}

