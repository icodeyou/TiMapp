package com.timappweb.timapp.activities;

import android.support.test.espresso.Espresso;
import android.util.Log;

import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.auth.AuthProviderInterface;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.fixtures.UsersFixture;
import com.timappweb.timapp.rest.io.responses.RestFeedback;
import com.timappweb.timapp.utils.SystemAnimations;
import com.timappweb.timapp.utils.TestUtil;
import com.timappweb.timapp.utils.annotations.AuthState;
import com.timappweb.timapp.utils.annotations.ConfigState;
import com.timappweb.timapp.utils.annotations.CreateAuthAction;
import com.timappweb.timapp.utils.annotations.CreateConfigAction;
import com.timappweb.timapp.utils.facebook.FacebookApiHelper;
import com.timappweb.timapp.utils.idlingresource.ApiCallIdlingResource;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.utils.mocklocations.AbstractMockLocationProvider;
import com.timappweb.timapp.utils.mocklocations.MockFusedLocationProvider;

import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Stephane on 24/09/2016.
 */
public class AbstractActivityTest {

    private static final String TAG = "AbstractActivityTest";
    private SystemAnimations mSystemAnimations;
    private ApiCallIdlingResource mApiCallIdlingResource;
    private AbstractMockLocationProvider mMockLocationProvider;

    @Rule
    public TestAnnotated testAnnoted = new TestAnnotated();

    public void beforeTest(){
        FacebookApiHelper.init();


        CreateConfigAction createConfigAction = testAnnoted.getCreateConfigAction();
        if (createConfigAction != null){
            if (createConfigAction.replaceIfExists()){
                ConfigurationProvider.clearAll();
            }
            if (!ConfigurationProvider.hasFullConfiguration()){
                ConfigurationProvider
                        .load(MyApplication.getApplicationBaseContext())
                        .execute();
            }
        }
        if (testAnnoted.getCreateAuthAction() != null){
            CreateAuthAction createAuthAction = testAnnoted.getCreateAuthAction();
            if (!MyApplication.isLoggedIn() || createAuthAction.replaceIfExists()){
                UsersFixture.init();

                MyApplication
                        .getAuthManager()
                        .getProvider(createAuthAction.providerId())
                        .login(UsersFixture.getLoginPayload(createAuthAction.payloadId()), new AuthProviderInterface.AuthAttemptCallback<RestFeedback>() {
                            @Override
                            public void onSuccess(RestFeedback feedback) {
                                assertTrue("User must be logged in", MyApplication.isLoggedIn());
                            }

                            @Override
                            public void onFailure(Throwable exception) {
                                assertTrue("Cannot login user to execute the test... Error: " + exception.toString(), false);
                            }
                        });
                TestUtil.sleep(10000);
            }
        }

        if (testAnnoted.getAuthState() != null){
            assertTrue("User must be logged in to perform this test",
                    testAnnoted.getAuthState().check() != AuthState.LoginState.YES || MyApplication.isLoggedIn());
            if (testAnnoted.getAuthState().check() == AuthState.LoginState.NO){
                MyApplication.getAuthManager().logout();
                assertTrue("User must NOT be logged in to perform this test", !MyApplication.isLoggedIn());
            }
        }

        if (testAnnoted.getConfigState() != null){
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
        if (mMockLocationProvider == null){
            mMockLocationProvider = MockFusedLocationProvider.create(LocationManager.getLocationProvider().getGoogleApiClient());
        }
        return mMockLocationProvider;
    }

    public class TestAnnotated extends TestWatcher {

        private AuthState authState;
        private ConfigState configState;
        private CreateAuthAction createAuthAction;
        private CreateConfigAction createConfigAction;

        @Override
        protected void starting( Description description) {
            authState = description.getAnnotation( AuthState.class);
            configState = description.getAnnotation( ConfigState.class);
            createAuthAction = description.getAnnotation( CreateAuthAction.class);
            createConfigAction = description.getAnnotation( CreateConfigAction.class);
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
    }
}
