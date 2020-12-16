package com.ashomok.lullabies.utils.rate_app;

import android.content.Context;
import android.content.SharedPreferences;

import com.ashomok.lullabies.R;
import com.ashomok.lullabies.ui.MyViewPagerAdapter;
import com.ashomok.lullabies.utils.LogHelper;

import javax.inject.Inject;

/**
 * Created by iuliia on 10/5/16.
 */

public class RateAppAskerImpl implements RateAppAsker {

    /**
     * Ask to rate app if the app was used RATE_APP_COUNT times
     */
    public static final int RATE_APP_COUNT = 70;
    public static final int NEVER_ASK = -1;
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private static final String TAG = LogHelper.makeLogTag(RateAppAskerImpl.class);
    private RateAppAskerCallback callback;

    @Inject
    public RateAppAskerImpl(SharedPreferences sharedPreferences, Context context) {
        this.sharedPreferences = sharedPreferences;
        this.context = context;
    }

    @Override
    public void init(RateAppAskerCallback callback) {
        this.callback = callback;

        int timesAppWasUsed = sharedPreferences.getInt(context.getString(R.string.times_app_was_used), 0);
        LogHelper.d(TAG, "adapter shows views count: " + timesAppWasUsed);

        if (timesAppWasUsed != NEVER_ASK) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (timesAppWasUsed >= RATE_APP_COUNT) {
                askToRate();
                editor.putInt(context.getString(R.string.times_app_was_used), 0);
            } else {
                editor.putInt(context.getString(R.string.times_app_was_used), ++timesAppWasUsed);
            }
            editor.apply();
        }
    }

    private void askToRate() {
        RateAppDialog1Fragment rateAppDialog1Fragment = RateAppDialog1Fragment.newInstance();
        rateAppDialog1Fragment.setRateAppAsker(this);
        callback.showDialogFragment(rateAppDialog1Fragment);
    }

    @Override
    public void onStopAsk() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(context.getString(R.string.times_app_was_used), NEVER_ASK);
        editor.apply();
    }

    @Override
    public void onEnjoyAppClicked() {
        RateAppDialog2Fragment rateAppDialog2Fragment = RateAppDialog2Fragment.newInstance();
        rateAppDialog2Fragment.setRateAppAsker(this);
        callback.showDialogFragment(rateAppDialog2Fragment);
    }
}
