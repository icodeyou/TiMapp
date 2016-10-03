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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.auth.AuthProviderInterface;
import com.timappweb.timapp.auth.FacebookAuthProvider;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.rest.io.responses.RestFeedback;

import java.io.IOException;


/**
 * NewActivity login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity{

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

    private View layoutFb;
    private View progressView;
    private LoginButton loginButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_login);

        layoutFb = findViewById(R.id.layout_fb);
        progressView = findViewById(R.id.progress_view);
        loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        mAuth = FirebaseAuth.getInstance();

        initFacebookButton();

        Button skipLogin = (Button) findViewById(R.id.skip_loggin_button);
        skipLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.home(LoginActivity.this);
            }
        });

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
        };
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
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void initFacebookButton() {
        // Initialize Facebook Login button
        CallbackManager mCallbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions("email", "public_profile", "user_friends"); // TODO params
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                LoginActivity.this.handleFacebookAccessToken(loginResult.getAccessToken());
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
    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        setProgressVisibility(true);

        AuthCredential credential = com.google.firebase.auth.FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Cannot login with firebase", task.getException());
                            Toast.makeText(LoginActivity.this, R.string.cannot_facebook_login,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JsonObject payload = FacebookAuthProvider.createPayload(token.getToken(),
                                task.getResult().getUser().getUid());
                        handleServerLogin(payload);
                    }
                });
    }
    private void handleServerLogin(JsonObject payload) {

        MyApplication
                .getAuthManager()
                .getProvider(FacebookAuthProvider.PROVIDER_ID)
                .login(payload, new AuthProviderInterface.AuthAttemptCallback<RestFeedback>() {

                    @Override
                    public void onSuccess(RestFeedback feedback) {
                        IntentsUtils.redirectToLastActivity(LoginActivity.this);
                        LoginActivity.this.finish();
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        FirebaseAuth.getInstance().signOut();
                        if (exception instanceof IOException){
                            setProgressVisibility(false);
                            Toast.makeText(LoginActivity.this, R.string.no_network_access, Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Log.e(TAG, "User attempt to connect with wrong facebook token");
                            setProgressVisibility(false);
                            Toast.makeText(LoginActivity.this, R.string.cannot_facebook_login, Toast.LENGTH_LONG).show();
                        }
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

