package com.ashomok.lullabies.utils.rate_app;

import com.ashomok.lullabies.ui.MyViewPagerAdapter;

/**
 * Created by iuliia on 10/5/16.
 */

public interface RateAppAsker {
    void init(RateAppAskerCallback callback);
    void onStopAsk();
    void onEnjoyAppClicked();
}
