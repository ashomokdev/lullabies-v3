package com.ashomok.lullabies.ui.full_screen_player_activity;

import android.support.annotation.StringRes;

import com.ashomok.lullabies.billing.model.SkuRowData;
import com.ashomok.lullabies.di_dagger.BasePresenter;
import com.ashomok.lullabies.ui.music_player_activity.MusicPlayerContract;

public class FullScreenPlayerContract {
    interface View {
    }

    interface Presenter extends BasePresenter<FullScreenPlayerContract.View> {
    }
}
