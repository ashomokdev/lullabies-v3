package com.ashomok.lullabies.ui.main_activity;

import android.support.v4.media.MediaBrowserCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.ashomok.lullabies.R;
import com.ashomok.lullabies.Settings;
import com.ashomok.lullabies.billing_kotlin.localdb.AdsFreeForever;
import com.ashomok.lullabies.billing_kotlin.localdb.AugmentedSkuDetails;
import com.ashomok.lullabies.billing_kotlin.viewmodels.BillingViewModel;
import com.ashomok.lullabies.utils.LogHelper;
import com.ashomok.lullabies.utils.NetworkHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

import static android.view.Menu.NONE;

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
                        view.updateView(Settings.isAdsActive);
                    }
                }
            });


            billingViewModel.getInappSkuDetailsListLiveData()
                    .observe(activity, new Observer<List<AugmentedSkuDetails>>() {
                        @Override
                        public void onChanged(List<AugmentedSkuDetails> augmentedSkuDetails) {
                            if (augmentedSkuDetails != null) {
                                if (augmentedSkuDetails.size() == 1) {
                                    removeAdsSkuRow = augmentedSkuDetails.get(0);
                                    LogHelper.d(TAG, "init sku row "
                                            + removeAdsSkuRow.toString());
                                } else {
                                    LogHelper.e(TAG,
                                            "unepected sku list size, expected 1, actual "
                                                    + augmentedSkuDetails.size());
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public void initCategoriesList(String rootMediaId, MediaBrowserCompat mediaBrowser) {
        Completable loadCategoriesComplatable = Completable.create(emitter ->
                mediaBrowser.subscribe(rootMediaId,
                        new MediaBrowserCompat.SubscriptionCallback() {
                            @Override
                            public void onChildrenLoaded(@NonNull String parentId,
                                                         @NonNull List<MediaBrowserCompat.MediaItem> children) {
                                try {
                                    LogHelper.d(TAG, "fragment onChildrenLoaded, parentId=" + parentId +
                                            "  count=" + children.size());
                                    checkForUserVisibleErrors(children.isEmpty());

                                    List<String> menuTitles = new ArrayList<>();
                                    for (int i = 0; i < children.size(); i++) {
                                    menuTitles.add(String.valueOf(
                                            children.get(i).getDescription().getTitle()));
                                    }
                                    view.addMenuItems(menuTitles);
                                    view.setCategories(children);

                                    emitter.onComplete();
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

        loadCategoriesComplatable
                .doOnSubscribe(disposable -> {
                    // Unsubscribing before subscribing is required if this mediaId already has a subscriber
                    // on this MediaBrowser instance. Subscribing to an already subscribed mediaId will replace
                    // the callback, but won't trigger the initial callback.onChildrenLoaded.
                    //
                    // This is temporary: A bug is being fixed that will make subscribe
                    // consistently call onChildrenLoaded initially, no matter if it is replacing an existing
                    // subscriber or not. Currently this only happens if the mediaID has no previous
                    // subscriber or if the media content changes on the service side, so we need to
                    // unsubscribe first.
                    mediaBrowser.unsubscribe(rootMediaId);
                })
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    view.browseCategory();

                }, throwable -> {
                    LogHelper.e(TAG, throwable, "Error from loading media");
                    checkForUserVisibleErrors(true);
                });
    }


    private void checkForUserVisibleErrors(boolean emptyResult) {
        if (emptyResult) {
            LogHelper.e(TAG, "Loading media returns empty result");
        }
        //todo
    }

    @Override
    public void dropView() {
        view = null;
    }
}
