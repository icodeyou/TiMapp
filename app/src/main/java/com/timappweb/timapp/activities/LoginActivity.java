package com.timappweb.timapp.activities;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.iid.InstanceID;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.data.entities.SocialProvider;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.RequestFailureCallback;
import com.timappweb.timapp.rest.io.responses.RestFeedback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;


/**
 * NewActivity login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity implements LoaderCallbacks<Cursor> {

    private static final String TAG = "LoginActivity";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    //private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
//    private EditText mPasswordView;
//    private View mProgressView;
//    private View mLoginFormView;

    private SimpleFacebook mSimpleFacebook;
    private View layoutFb;
    private View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        layoutFb = findViewById(R.id.layout_fb);
        progressView = findViewById(R.id.progress_view);

        /*// Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.remote_id.email);

        mPasswordView = (EditText) findViewById(R.remote_id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int remote_id, KeyEvent keyEvent) {
                if (remote_id == R.remote_id.login || remote_id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        // Prefix for testing purpose
        if (BuildConfig.DEBUG){
            mEmailView.setText("t@m.com");
            mPasswordView.setText("test");
        }

        Button mEmailSignInButton = (Button) findViewById(R.remote_id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        mLoginFormView = findViewById(R.remote_id.sign_up_form);
        mProgressView = findViewById(R.remote_id.login_progress);
*/
        setListeners();

        final Activity that = this;
        Button skipLogin = (Button) findViewById(R.id.skip_loggin_button);
        skipLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.home(that);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(this.isTaskRoot()) {
            IntentsUtils.home(this);
        } else {
            super.onBackPressed();
        }
    }

    private void setListeners() {
        ImageButton loginButton = (ImageButton) findViewById(R.id.facebook_login_button);
        final OnLoginListener onLoginListener = new OnLoginListener() {

            @Override
            public void onLogin(final String accessToken, List<Permission> acceptedPermissions, List<Permission> declinedPermissions) {
                Log.i(TAG, "Logging in");
                setProgressVisibility(true);
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("access_token", accessToken);
                params.put("app_id", InstanceID.getInstance(LoginActivity.this).getId());

                Call<RestFeedback> call = RestClient.service().facebookLogin(params);
                RestClient.buildCall(call)
                        //.onResponse(new AutoMergeCallback(user))
                        .onResponse(new HttpCallback<RestFeedback>() {
                            @Override
                            public void successful(RestFeedback feedback) {
                                try{
                                    int userId = Integer.parseInt(feedback.data.get("id"));
                                    User user = User.loadByRemoteId(User.class, userId);
                                    if (user == null) user = new User();

                                    String token = feedback.data.get("token");
                                    user.username = feedback.data.get("username");
                                    user.provider_uid = feedback.data.get("social_id");
                                    user.provider = SocialProvider.FACEBOOK;
                                    user.remote_id = Integer.parseInt(feedback.data.get("id"));
                                    user.app_id = InstanceID.getInstance(LoginActivity.this).getId();
                                    //MyApplication.updateGoogleMessagingToken(LoginActivity.this);
                                    Log.i(TAG, "Trying to login user: " + user);
                                    MyApplication.login(getApplicationContext(), user, token, accessToken);
                                    MyApplication.requestGcmToken(LoginActivity.this);
                                    IntentsUtils.lastActivityBeforeLogin(LoginActivity.this);

                                    QuotaManager.sync();
                                }
                                catch (Exception ex){
                                    Log.e(TAG, "Cannot parse server response for login: " + ex.getMessage());
                                    ex.printStackTrace();
                                    Toast.makeText(LoginActivity.this, R.string.error_server_unavailable, Toast.LENGTH_LONG).show();
                                    setProgressVisibility(false);
                                }
                            }

                            @Override
                            public void notSuccessful() {
                                setProgressVisibility(false);
                                Log.i(TAG, "User attempt to connect with wrong credential");
                                Toast.makeText(LoginActivity.this, R.string.cannot_facebook_login, Toast.LENGTH_LONG).show();
                            }
                        })
                        .onError(new RequestFailureCallback(){
                            @Override
                            public void onError(Throwable error) {
                                setProgressVisibility(false);
                                Toast.makeText(LoginActivity.this, R.string.no_network_access, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .perform();
            }

            @Override
            public void onCancel() {
                setProgressVisibility(false);
                Log.d(TAG, "Facebook connection canceled");
            }

            @Override
            public void onFail(String reason) {
                setProgressVisibility(false);
                Log.d(TAG, "Facebook connection failed : " + reason);
            }

            @Override
            public void onException(Throwable throwable) {
                setProgressVisibility(false);
                Log.d(TAG, "Facebook connection.. OnException: " + throwable.getMessage());
            }

        };

        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutFb.setVisibility(View.GONE);
                mSimpleFacebook.login(onLoginListener);
            }
        });
    }


    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    /*public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            String loginStr = email+":"+password;
            if (BuildConfig.DEBUG &&
                    Arrays.asList(DUMMY_CREDENTIALS).contains(loginStr)){
                // Check dummy credential
                Log.i(TAG, "Login with dummy credential");
                RestClient.instance().createLoginSession("NewActivity", new User(email, ""));
                IntentsUtils.home(this);
            }
            else{
                //showProgress(true);
                mAuthTask = new UserLoginTask(this);
                mAuthTask.execute((Void) null);
            }
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return true; //email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > MINMUM_PASSWORD_LENGTH;
    }

    *//**
     * Shows the progress UI and hides the login form.
     *//*
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent) || isTaskRoot()) {

                    TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(upIntent)
                            .startActivities();
                } else {
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSimpleFacebook = SimpleFacebook.getInstance(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mSimpleFacebook.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private void setProgressVisibility(boolean bool) {
        if(bool) {
            progressView.setVisibility(View.VISIBLE);
            layoutFb.setVisibility(View.GONE);
        } else {
            progressView.setVisibility(View.GONE);
            layoutFb.setVisibility(View.VISIBLE);
        }
    }
}

