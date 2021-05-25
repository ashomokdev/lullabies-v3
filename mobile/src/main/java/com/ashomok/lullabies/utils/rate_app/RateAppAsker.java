package com.ashomok.lullabies.utils.rate_app;

/**
 * Created by iuliia on 10/5/16.
 */

public interface RateAppAsker {
    void count(RateAppAskerCallback callback);
    void onStopAsk();
    void onEnjoyAppClicked();
}
