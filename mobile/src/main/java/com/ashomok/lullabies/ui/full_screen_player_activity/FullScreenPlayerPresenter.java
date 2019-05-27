package com.ashomok.lullabies.ui.full_screen_player_activity;

import android.content.Context;
import android.support.annotation.Nullable;

import com.ashomok.lullabies.R;
import com.ashomok.lullabies.Settings;
import com.ashomok.lullabies.billing.BillingProviderCallback;
import com.ashomok.lullabies.billing.BillingProviderImpl;
import com.ashomok.lullabies.billing.model.SkuRowData;
import com.ashomok.lullabies.ui.music_player_activity.MusicPlayerContract;
import com.ashomok.lullabies.ui.music_player_activity.MusicPlayerPresenter;
import com.ashomok.lullabies.utils.LogHelper;
import com.ashomok.lullabies.utils.NetworkHelper;

import java.util.List;

import javax.inject.Inject;

import static com.ashomok.lullabies.billing.BillingProviderImpl.ADS_FREE_FOREVER_SKU_ID;

public class FullScreenPlayerPresenter implements FullScreenPlayerContract.Presenter {
    public static final String TAG = LogHelper.makeLogTag(MusicPlayerPresenter.class);

    @Nullable
    public FullScreenPlayerContract.View view;

    private Context context;

    /**
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     */
    @Inject
    FullScreenPlayerPresenter(Context context, BillingProviderImpl billingProvider) {
        this.context = context;
    }

    @Override
    public void takeView(FullScreenPlayerContract.View activity) {
        view = activity;
        init();
    }

    private void init() {
       //nothing
    }

    @Override
    public void dropView() {
        view = null;
    }
}
