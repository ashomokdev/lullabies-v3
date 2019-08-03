package com.ashomok.lullabies.ui.main_activity;

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

import java.util.List;

import javax.inject.Inject;

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
    public void dropView() {
        view = null;
    }
}
