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

public class RateAppDialog2Fragment extends DialogFragment {

    private RateAppAsker rateAppAsker;

    private Button noBtn;
    private Button okBtn;

    public RateAppDialog2Fragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static RateAppDialog2Fragment newInstance() {
        return new RateAppDialog2Fragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return inflater.inflate(R.layout.rate_app_dialog_2_layout, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noBtn = view.findViewById(R.id.no);
        okBtn = view.findViewById(R.id.ok);

        noBtn.setOnClickListener(view1 -> onNoBtnClicked());
        okBtn.setOnClickListener(view1 -> onOkBtnClicked());
    }

    private void onOkBtnClicked() {
        rate();
        rateAppAsker.onStopAsk();
        dismiss();
    }

    private void onNoBtnClicked() {
        dismiss();
    }

    public void setRateAppAsker(RateAppAsker rateAppAsker) {
        this.rateAppAsker = rateAppAsker;
    }

    private void rate() {
        RateAppUtil rateAppUtil = new RateAppUtil();
        rateAppUtil.rate(getActivity());
    }
}
