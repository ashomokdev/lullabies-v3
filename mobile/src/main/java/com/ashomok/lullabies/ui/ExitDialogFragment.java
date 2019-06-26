package com.ashomok.lullabies.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

import com.ashomok.lullabies.MusicService;
import com.ashomok.lullabies.R;
import com.ashomok.lullabies.utils.StartServiceUtil;

/**
 * Created by iuliia on 7/10/16.
 */
public class ExitDialogFragment extends DialogFragment {

    public static ExitDialogFragment newInstance(int title) {
        ExitDialogFragment frag = new ExitDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setPositiveButton(R.string.ok,
                        (dialog, whichButton) -> {
                            ActivityCompat.finishAffinity(getActivity());

                            //stop music service
                            Intent i = new Intent(getActivity(), MusicService.class);
                            i.setAction(MusicService.ACTION_CMD);
                            i.putExtra(MusicService.CMD_NAME, MusicService.CMD_STOP);
                            StartServiceUtil.startService(getActivity(), i);
                        }
                )
                .setNegativeButton(R.string.cancel,
                        (dialog, whichButton) -> {
                            //nothing
                        }
                )
                .create();
    }
}
