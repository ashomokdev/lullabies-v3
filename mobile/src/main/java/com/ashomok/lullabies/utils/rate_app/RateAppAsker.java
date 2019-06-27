package com.ashomok.lullabies.utils.rate_app;

import android.content.Context;
import android.content.SharedPreferences;

import com.ashomok.lullabies.R;
import com.ashomok.lullabies.utils.LogHelper;

import javax.inject.Inject;


/**
 * Created by iuliia on 10/5/16.
 */

public class RateAppAsker implements OnNeverAskReachedListener {

    /**
     * Ask to rate app if the app was used RATE_APP_COUNT times
     */
    public static final int RATE_APP_COUNT = 100;
    public static final int NEVER_ASK = -1;
    private final Context context;
    private SharedPreferences sharedPreferences;
    private static final String TAG = LogHelper.makeLogTag(RateAppAsker.class);


    @Inject
    public RateAppAsker(SharedPreferences sharedPreferences, Context context) {
        this.sharedPreferences = sharedPreferences;
        this.context = context;
    }

    public void init(RateAppAskerCallback callback) {
        int timesAppWasUsed = sharedPreferences.getInt(context.getString(R.string.times_app_was_used), 0);
        LogHelper.d(TAG, "adapter shows views count: " + timesAppWasUsed);

        if (timesAppWasUsed != NEVER_ASK) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (timesAppWasUsed >= RATE_APP_COUNT) {
                askToRate(callback);
                editor.putInt(context.getString(R.string.times_app_was_used), 0);
            } else {

                editor.putInt(context.getString(R.string.times_app_was_used), ++timesAppWasUsed);
            }
            editor.apply();
        }
    }

    private void askToRate(RateAppAskerCallback callback) {
        RateAppDialogFragment rateAppDialogFragment = RateAppDialogFragment.newInstance();
        rateAppDialogFragment.setOnStopAskListener(this);
        callback.showRateAppDialog(rateAppDialogFragment);
    }

    @Override
    public void onStopAsk() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(context.getString(R.string.times_app_was_used), NEVER_ASK);
        editor.apply();
    }
}
