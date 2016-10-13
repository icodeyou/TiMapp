package com.timappweb.timapp.activities;

import android.app.Activity;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.auth.AuthManager;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.fixtures.UsersFixture;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.utils.SystemAnimations;
import com.timappweb.timapp.utils.TestUtil;
import com.timappweb.timapp.utils.annotations.AuthState;
import com.timappweb.timapp.utils.annotations.ClearAuth;
import com.timappweb.timapp.utils.annotations.ClearConfig;
import com.timappweb.timapp.utils.annotations.ClearFirstStart;
import com.timappweb.timapp.utils.annotations.ConfigState;
import com.timappweb.timapp.utils.annotations.CreateAuthAction;
import com.timappweb.timapp.utils.annotations.CreateConfigAction;
import com.timappweb.timapp.utils.annotations.CreateLastLaunch;
import com.timappweb.timapp.utils.facebook.FacebookApiHelper;
import com.timappweb.timapp.utils.idlingresource.ApiCallIdlingResource;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.utils.mocklocations.AbstractMockLocationProvider;
import com.timappweb.timapp.utils.mocklocations.MockFusedLocationProvider;

import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import retrofit2.Call;
import retrofit2.Response;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Stephane on 24/09/2016.
 */
public class AbstractActivityTest {

    private static final String TAG = "AbstractActivityTest";
    private static final int MAX_ITERATION_WAIT_FOR_FINE_LOCATION = 20;
    private SystemAnimations mSystemAnimations;
    private ApiCallIdlingResource mApiCallIdlingResource;
    private AbstractMockLocationProvider mMockLocationProvider;

    @Rule
    public TestAnnotated testAnnoted = new TestAnnotated();

    public void beforeTest() {
        FacebookApiHelper.init();

        if (testAnnoted.getClearAuth() != null) {
            MyApplication.logout();
        }
        if (testAnnoted.getClearFirstStart() != null) {
            MyApplication.clearStoredData();
        }
        if (testAnnoted.getClearConfig() != null) {
            ConfigurationProvider.clearAll();
        }

        CreateConfigAction createConfigAction = testAnnoted.getCreateConfigAction();
        if (createConfigAction != null) {
            if (createConfigAction.replaceIfExists()) {
                ConfigurationProvider.clearAll();
            }
            if (!ConfigurationProvider.hasFullConfiguration()) {
                ConfigurationProvider
                        .load(MyApplication.getApplicationBaseContext())
                        .execute();
            }
            assertTrue("Cannot load full app configuration from the server: ", ConfigurationProvider.hasFullConfiguration());
        }

        if (testAnnoted.getCreateLastLaunch() != null){
            MyApplication.updateLastLaunch();
        }

        if (testAnnoted.getCreateAuthAction() != null) {
            CreateAuthAction createAuthAction = testAnnoted.getCreateAuthAction();
            if (!MyApplication.isLoggedIn() || createAuthAction.replaceIfExists()) {
                UsersFixture.init();
                final JsonObject loginPayload = UsersFixture.getLoginPayload(createAuthAction.payloadId());
                Log.i(TAG, "@BeforeTest: Login with payload: " + loginPayload);
                MyApplication
                        .getAuthManager()
                        .logWith(new AuthManager.LoginMethod<JsonObject, String>() {
                            @Override
                            public Call<JsonObject> login(JsonObject data) {
                                return RestClient.service().facebookLogin(data);
                            }

                            @Override
                            public void cancelLogin() {

                            }

                            @Override
                            public void onCurrentAccessTokenChanged(String oldAccessToken, String currentAccessToken) {

                            }

                            @Override
                            public void onPermissionRevoked() {

                            }

                            @Override
                            public String getAccessToken() throws AuthManager.NoProviderAccessTokenException {
                                return loginPayload.get("access_token").getAsString();
                            }
                        }, loginPayload)
                        .onFinally(new HttpCallManager.FinallyCallback() {
                            @Override
                            public void onFinally(Response response, Throwable error) {
                                assertTrue("User must be logged in", MyApplication.isLoggedIn());
                            }
                        });
                TestUtil.sleep(10000);
            }
        }

        if (testAnnoted.getAuthState() != null) {
            assertTrue("User must be logged in to perform this test",
                    testAnnoted.getAuthState().logging() != AuthState.LoginState.YES || MyApplication.isLoggedIn());
            if (testAnnoted.getAuthState().logging() == AuthState.LoginState.NO) {
                MyApplication.getAuthManager().logout();
                assertTrue("User must NOT be logged in to perform this test", !MyApplication.isLoggedIn());
            }
        }

        if (testAnnoted.getConfigState() != null) {
            assertTrue("Rules should be loaded in app state to perform this test",
                    ConfigurationProvider.hasRulesConfig() == testAnnoted.getConfigState().rules());
            assertTrue("Event categories should be loaded in app state to perform this test",
                    ConfigurationProvider.hasEventCategoriesConfig() == testAnnoted.getConfigState().eventCategories());
            assertTrue("Spot categories should be loaded in app state to perform this test",
                    ConfigurationProvider.hasSpotCategoriesConfig() == testAnnoted.getConfigState().spotCategories());
        }

    }

    public void systemAnimations(boolean enabled){
        mSystemAnimations = new SystemAnimations(getInstrumentation().getContext());
        mSystemAnimations.disableAll();
    }

    public void idlingApiCall() {
        mApiCallIdlingResource = new ApiCallIdlingResource();
        Espresso.registerIdlingResources(mApiCallIdlingResource);
    }

    public void resetAsBeforeTest() {
        if (mSystemAnimations != null){
            mSystemAnimations.enableAll();
        }
        if (mApiCallIdlingResource != null){
            Espresso.unregisterIdlingResources(mApiCallIdlingResource);
        }
    }

    public AbstractMockLocationProvider getMockLocationProvider() {
        assertTrue("This activity does not seem to have started the LocationManager. Add call LocationManager.start(this) in the @onStart() of the current activity",
                LocationManager.getLocationProvider() != null);
        if (mMockLocationProvider == null){
            mMockLocationProvider = MockFusedLocationProvider.create(LocationManager.getLocationProvider().getGoogleApiClient());

        }
        return mMockLocationProvider;
    }


    protected void waitForFineLocation(ActivityTestRule<? extends Activity> mActivityRule) {
        int i = 0;
        while (!LocationManager.hasFineLocation()){
            TestUtil.sleep(100);
            i++;
            if (i > MAX_ITERATION_WAIT_FOR_FINE_LOCATION){
                Log.e(TAG, "Cannot find user location !");
                break;
            }
        }

    }

    //public class TestSuiteAnnoted extends TestWatcher{}

    public class TestAnnotated extends TestWatcher {

        private AuthState authState;
        private ConfigState configState;
        private CreateAuthAction createAuthAction;
        private CreateConfigAction createConfigAction;
        private ClearAuth clearAuth;
        private ClearConfig clearConfig;
        private ClearFirstStart clearFirstStart;
        private CreateLastLaunch createLastLaunch;

        @Override
        protected void starting( Description description) {
            authState = description.getAnnotation( AuthState.class);
            configState = description.getAnnotation( ConfigState.class);
            createAuthAction = description.getAnnotation( CreateAuthAction.class);
            createConfigAction = description.getAnnotation( CreateConfigAction.class);
            clearAuth = description.getAnnotation( ClearAuth.class);
            clearConfig = description.getAnnotation( ClearConfig.class);
            clearFirstStart = description.getAnnotation( ClearFirstStart.class);
            createLastLaunch = description.getAnnotation( CreateLastLaunch.class);
        }

        public AuthState getAuthState() {
            return authState;
        }

        public ConfigState getConfigState() {
            return configState;
        }

        public CreateAuthAction getCreateAuthAction() {
            return createAuthAction;
        }

        public CreateConfigAction getCreateConfigAction() {
            return createConfigAction;
        }

        public ClearAuth getClearAuth() {
            return clearAuth;
        }

        public ClearConfig getClearConfig() {
            return clearConfig;
        }

        public ClearFirstStart getClearFirstStart() {
            return clearFirstStart;
        }

        public CreateLastLaunch getCreateLastLaunch() {
            return createLastLaunch;
        }
    }
}
