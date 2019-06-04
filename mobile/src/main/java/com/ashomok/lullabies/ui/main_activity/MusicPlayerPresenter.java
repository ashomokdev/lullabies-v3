package com.ashomok.lullabies.ui.main_activity;

import android.content.Context;
import android.support.annotation.Nullable;

import com.ashomok.lullabies.R;
import com.ashomok.lullabies.Settings;
import com.ashomok.lullabies.billing.BillingProvider;
import com.ashomok.lullabies.billing.BillingProviderCallback;
import com.ashomok.lullabies.billing.BillingProviderImpl;
import com.ashomok.lullabies.billing.model.SkuRowData;
import com.ashomok.lullabies.utils.LogHelper;
import com.ashomok.lullabies.utils.NetworkHelper;

import java.util.List;

import javax.inject.Inject;

import static com.ashomok.lullabies.billing.BillingProviderImpl.ADS_FREE_FOREVER_SKU_ID;

public class MusicPlayerPresenter implements MusicPlayerContract.Presenter {
    public static final String TAG = LogHelper.makeLogTag(MusicPlayerPresenter.class);

    @Nullable
    public MusicPlayerContract.View view;

    private Context context;
    private BillingProvider billingProvider;
    private SkuRowData removeAdsSkuRow;
    private BillingProviderCallback billingProviderCallback = new BillingProviderCallback() {
        @Override
        public void onPurchasesUpdated() {
            if (view != null) {
                Settings.isAdsActive = !billingProvider.isAdsFreeForever();
                view.updateView(Settings.isAdsActive);
            }
        }

        @Override
        public void showError(int stringResId) {
            if (view != null) {
                view.showError(stringResId);
            }
        }

        @Override
        public void showInfo(String message) {
            if (view != null) {
                view.showInfo(message);
            }
        }

        @Override
        public void onSkuRowDataUpdated() {
            initSkuRows(billingProvider.getSkuRowDataListForInAppPurchases());
        }
    };

    /**
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     */
    @Inject
    MusicPlayerPresenter(Context context, BillingProvider billingProvider) {
        this.context = context;
        this.billingProvider = billingProvider;
    }

    /**
     * update sku rows
     *
     * @param skuRowData
     */
    private void initSkuRows(List<SkuRowData> skuRowData) {
        if (view != null) {
            for (SkuRowData item : skuRowData) {
                LogHelper.d(TAG, "init sku row " + item.toString());
                switch (item.getSku()) {
                    case ADS_FREE_FOREVER_SKU_ID:
                        removeAdsSkuRow = item;
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void onRemoveAdsClicked() {
        if (removeAdsSkuRow != null) {
            billingProvider.getBillingManager().initiatePurchaseFlow(removeAdsSkuRow.getSku(),
                    removeAdsSkuRow.getSkuType());

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
        if (view != null) {
            if (!NetworkHelper.isOnline(context)) {
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
        billingProvider.init(billingProviderCallback);

        if (view != null) {
            checkConnection();
        }
    }

    @Override
    public void dropView() {
        view = null;
        billingProvider.destroy();
    }
}
