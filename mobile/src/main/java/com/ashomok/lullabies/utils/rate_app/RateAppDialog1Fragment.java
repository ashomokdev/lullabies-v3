package com.ashomok.lullabies.utils.rate_app;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.ashomok.lullabies.R;

/**
 * Created by iuliia on 10/5/16.
 */

public class RateAppDialog1Fragment extends DialogFragment {

    private RateAppAsker rateAppAsker;

    private Button notReallyBtn;
    private Button yesBtn;

    public RateAppDialog1Fragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static RateAppDialog1Fragment newInstance() {
        return new RateAppDialog1Fragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return inflater.inflate(R.layout.rate_app_dialog_1_layout, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notReallyBtn = view.findViewById(R.id.not_really);
        yesBtn = view.findViewById(R.id.yes);

        notReallyBtn.setOnClickListener(view1 -> onNotReallyBtnClicked());
        yesBtn.setOnClickListener(view1 -> onYesBtnClicked());
    }

    private void onYesBtnClicked() {
        rateAppAsker.onEnjoyAppClicked();
        dismiss();
    }

    private void onNotReallyBtnClicked() {
        rateAppAsker.onStopAsk();
        dismiss();
    }

    public void setRateAppAsker(RateAppAsker rateAppAsker) {
        this.rateAppAsker = rateAppAsker;
    }
}
