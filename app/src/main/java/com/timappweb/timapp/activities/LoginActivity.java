package com.timappweb.timapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.auth.AuthManager;
import com.timappweb.timapp.auth.FacebookLoginProvider;
import com.timappweb.timapp.auth.FirebaseAuthProvider;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.rest.managers.HttpCallManager;

import retrofit2.Response;


/**
 * NewActivity localLogin screen that offers localLogin via email/password.
 */
public class LoginActivity extends BaseActivity implements AuthManager.AuthStateChangedListener
        //implements FirebaseAuthProvider.FirebaseLoginCallback
        {

    private static final String TAG = "LoginActivity";

    /**
     * Keep track of the localLogin task to ensure we can cancel it if requested.
     */
    private View layoutFb;
    private View progressView;
    private LoginButton loginButton;
    //private FirebaseAuth.AuthStateListener mAuthListener;
    private CallbackManager mFacebookCallbackManager;
    //private FirebaseAuthProvider firebaseAuthProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_login);

        layoutFb = findViewById(R.id.layout_fb);
        progressView = findViewById(R.id.progress_view);
        loginButton = (LoginButton) findViewById(R.id.facebook_login_button);


        initFacebookButton();

        Button skipLogin = (Button) findViewById(R.id.skip_loggin_button);
        skipLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.home(LoginActivity.this);
            }
        });

        /*
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };*/
        //firebaseAuthProvider = new FirebaseAuthProvider().setCallback(this);
    }

    @Override
    public void onBackPressed() {
        if(this.isTaskRoot()) {
            IntentsUtils.home(this);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //mAuth.addAuthStateListener(mAuthListener);
        MyApplication.getAuthManager().registerListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        MyApplication.getAuthManager().removeListener(this);
        /*
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }*/
    }

    // ---------------------------------------------------------------------------------------------

    private void initFacebookButton() {
        // Initialize Facebook Login button
        mFacebookCallbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions("email", "public_profile", "user_friends"); // TODO params
        loginButton.registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onFirebaseLoginSuccess:" + loginResult);
                setProgressVisibility(true);
                MyApplication.getAuthManager()
                        .logWith(new FacebookLoginProvider(), loginResult)
                        .onFinally(new HttpCallManager.FinallyCallback() {
                            @Override
                            public void onFinally(Response response, Throwable error) {
                                setProgressVisibility(false);
                                if (error != null || !response.isSuccessful()){
                                    Toast.makeText(LoginActivity.this, R.string.cannot_facebook_login, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "Facebook connection canceled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "Facebook connection failed : " + error);
            }
        });
    }

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
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

    @Override
    public void onLogin() {
        IntentsUtils.redirectToLastActivity(this);
        finish();
    }

    @Override
    public void onLogout() {

    }

    /*
    @Override
    public void onFirebaseLoginSuccess(String providerId) {
        setProgressVisibility(false);
        IntentsUtils.redirectToLastActivity(this);
    }

    @Override
    public void onFirebaseLoginFailure(String providerId, Throwable exception) {
        setProgressVisibility(false);
    }*/
}

