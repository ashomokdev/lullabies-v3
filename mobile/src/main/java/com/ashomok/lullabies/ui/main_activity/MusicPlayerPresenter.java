package com.ashomok.lullabies.ui.main_activity;

import android.app.DialogFragment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.ashomok.lullabies.R;
import com.ashomok.lullabies.Settings;
import com.ashomok.lullabies.billing_kotlin.BillingRepository;
import com.ashomok.lullabies.billing_kotlin.localdb.AdsFreeForever;
import com.ashomok.lullabies.billing_kotlin.localdb.AugmentedSkuDetails;
import com.ashomok.lullabies.billing_kotlin.viewmodels.BillingViewModel;
import com.ashomok.lullabies.utils.LogHelper;
import com.ashomok.lullabies.utils.NetworkHelper;
import com.ashomok.lullabies.utils.rate_app.RateAppAsker;
import com.ashomok.lullabies.utils.rate_app.RateAppAskerCallback;
import com.ashomok.lullabies.utils.rate_app.RateAppUtil;

import java.util.List;

import javax.inject.Inject;

public class MusicPlayerPresenter implements MusicPlayerContract.Presenter, RateAppAskerCallback {
    public static final String TAG = LogHelper.makeLogTag(MusicPlayerPresenter.class);

    @Nullable
    public MusicPlayerContract.View view;
    private BillingViewModel billingViewModel;
    private AugmentedSkuDetails removeAdsSkuRow;
    private AppCompatActivity activity; //todo why not simple Activity?

    RateAppAsker rateAppAsker;

    @Inject
    MusicPlayerPresenter(RateAppAsker rateAppAsker) {
        this.rateAppAsker = rateAppAsker;
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

            rateAppAsker.count(this);

            billingViewModel = ViewModelProviders.of(activity).get(BillingViewModel.class); //todo fix deprecated https://startandroid.ru/ru/courses/architecture-components/27-course/architecture-components/527-urok-4-viewmodel.html

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
                                if (augmentedSkuDetails.size() > 0) {
                                    for (AugmentedSkuDetails item : augmentedSkuDetails) {
                                        if (item.getSku().equals(
                                                BillingRepository.AppSku.INSTANCE.getADS_FREE_FOREVER_SKU_ID())) {
                                            removeAdsSkuRow = item;
                                            LogHelper.d(TAG, "init sku row "
                                                    + removeAdsSkuRow.toString());
                                        }
                                    }
                                } else {
                                    LogHelper.e(TAG, "empty In App Billing SKU list size");
                                }
                            }
                        }
                    });



        }
    }

    @Override
    public void rateApp() {
        checkConnection();
        if (NetworkHelper.isOnline(activity)) {
            RateAppUtil rateAppUtil = new RateAppUtil();
            rateAppUtil.rate(activity);
        }
    }

    @Override
    public void dropView() {
        view = null;
    }

    @Override
    public void showDialogFragment(DialogFragment dialogFragment) {
        if (activity != null) {
            try {
                dialogFragment.show(activity.getFragmentManager(), "dialog");
            } catch (IllegalStateException exception) {
                //IllegalStateException “Can not perform this action after onSaveInstanceState” because of Timer here ignore
                LogHelper.d(TAG, "showDialogFragment failed because of IllegalStateException");
            }
        }
    }
}
