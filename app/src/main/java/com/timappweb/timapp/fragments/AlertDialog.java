package com.timappweb.timapp.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.timappweb.timapp.R;

/**
 * Created by stephane on 8/30/2015.
 */
public class AlertDialog extends DialogFragment {

    public static DialogFragment show(Activity act, int title){
        DialogFragment newFragment = newInstance(title);
        newFragment.show(act.getFragmentManager(), "AlertDialog");
        return newFragment;
    }

    public static AlertDialog newInstance(int title) {
        AlertDialog frag = new AlertDialog();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");

        return new android.app.AlertDialog.Builder(getActivity())
                //.setIcon(R.drawable.alert_dialog_icon)
                .setTitle(title)
                .setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Nothing
                            }
                        }
                )
                /*
                .setNegativeButton(R.string.alert_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((FragmentAlertDialog)getActivity()).doNegativeClick();
                            }
                        }
                )
                */
                .create();
    }
}