package com.timappweb.timapp.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

/**
 * Created by stephane on 8/29/2015.
 */
public abstract class ModalAsyncTask extends AsyncTask<String, Void, Boolean> {

    private ProgressDialog dialog;

    public ModalAsyncTask(Activity activity){
        this.dialog = new ProgressDialog(activity);
    }

    /** progress dialog to simpleMessage user that the backup is processing. */
    /** application context. */
    @Override
    protected void onPreExecute() {
        this.dialog.setMessage("Please wait");
        this.dialog.show();
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
