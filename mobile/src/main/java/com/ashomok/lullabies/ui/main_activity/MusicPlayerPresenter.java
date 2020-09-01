package com.ashomok.lullabies.ui.main_activity;

import android.support.v4.media.MediaBrowserCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.ashomok.lullabies.R;
import com.ashomok.lullabies.Settings;
import com.ashomok.lullabies.billing_kotlin.BillingRepository;
import com.ashomok.lullabies.billing_kotlin.localdb.AdsFreeForever;
import com.ashomok.lullabies.billing_kotlin.localdb.AugmentedSkuDetails;
import com.ashomok.lullabies.billing_kotlin.viewmodels.BillingViewModel;
import com.ashomok.lullabies.utils.LogHelper;
import com.ashomok.lullabies.utils.NetworkHelper;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;

public class MusicPlayerPresenter implements MusicPlayerContract.Presenter {
    public static final String TAG = LogHelper.makeLogTag(MusicPlayerPresenter.class);

    @Nullable
    public MusicPlayerContract.View view;
    private BillingViewModel billingViewModel;
    private AugmentedSkuDetails removeAdsSkuRow;
    private AppCompatActivity activity;

    @Inject
    MusicPlayerPresenter() {
    }

    @Override
    public void onRemoveAdsClicked() {
        if (removeAdsSkuRow != null) {
            billingViewModel.makePurchase(activity, removeAdsSkuRow);
            LogHelper.d(TAG, "starting purchase flow for SkuDetail "
                    + removeAdsSkuRow.toString());
        }
    }

    @Override
    public void proposeRemoveAds() {
        if (view != null) {
            checkConnection();
            if (removeAdsSkuRow != null) {
                view.showRemoveAdDialog(removeAdsSkuRow);
            }
        }
    }

    private void checkConnection() {
        if (view != null && activity != null) {
            if (!NetworkHelper.isOnline(activity)) {
                view.showError(R.string.no_internet_connection);
            }
        }
    }

    @Override
    public void takeView(MusicPlayerContract.View activity) {
        view = activity;
        init();
    }

    private void init() {
        if (view != null) {
            activity = view.getActivity();
            checkConnection();

            billingViewModel = ViewModelProviders.of(activity).get(BillingViewModel.class);

            billingViewModel.
                    getAdsFreeForeverLiveData().observe(activity, new Observer<AdsFreeForever>() {
                @Override
                public void onChanged(AdsFreeForever adsFreeForever) {
                    if (adsFreeForever != null) {
                        Settings.isAdsActive = adsFreeForever.mayPurchase();
                        view.updateViewForAd(Settings.isAdsActive);
                    }
                }
            });

            billingViewModel.getInappSkuDetailsListLiveData()
                    .observe(activity, new Observer<List<AugmentedSkuDetails>>() {
                        @Override
                        public void onChanged(List<AugmentedSkuDetails> augmentedSkuDetails) {
                            if (augmentedSkuDetails != null) {
                                if (augmentedSkuDetails.size() > 0 ) {
                                    for (AugmentedSkuDetails item : augmentedSkuDetails){
                                        if (item.getSku().equals(
                                                BillingRepository.AppSku.INSTANCE.getADS_FREE_FOREVER_SKU_ID())){
                                            removeAdsSkuRow = item;
                                            LogHelper.d(TAG, "init sku row "
                                                    + removeAdsSkuRow.toString());
                                        }
                                    }
                                } else {
                                    LogHelper.e(TAG, "empty sku list size");
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public Single<List<MediaBrowserCompat.MediaItem>> initMediaBrowserLoader(
            String rootMediaId, MediaBrowserCompat mediaBrowser) {
        return Single.create(emitter -> mediaBrowser.subscribe(rootMediaId,
                        new MediaBrowserCompat.SubscriptionCallback() {
                            @Override
                            public void onChildrenLoaded(@NonNull String parentId,
                                                         @NonNull List<MediaBrowserCompat.MediaItem> children) {
                                try {
                                    LogHelper.d(TAG, "onChildrenLoaded, parentId=" + parentId +
                                            "  count=" + children.size());
                                    checkForUserVisibleErrors(children.isEmpty());
                                    if (view != null) {
                                        view.addMenuItems(children);
                                    }
                                    emitter.onSuccess(children);
                                } catch (Throwable t) {
                                    LogHelper.e(TAG, "Error on childrenloaded ", t);
                                    emitter.onError(t);
                                }
                            }
                            @Override
                            public void onError(@NonNull String id) {
                                LogHelper.e(TAG, "browse fragment subscription onError, id=" + id);
                                emitter.onError(new Exception(id));
                            }
                        }));
    }

    private void checkForUserVisibleErrors(boolean emptyResult) {
        if (emptyResult) {
            if (view != null) {
                view.checkForUserVisibleErrors(true);
            }
            LogHelper.e(TAG, "Loading media returns empty result");
        }
    }

    @Override
    public void dropView() {
        view = null;
    }
}
