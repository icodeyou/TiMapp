package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Context;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.auth.AuthManager;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.models.MyModel;
import com.timappweb.timapp.fixtures.UsersFixture;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.utils.SystemAnimations;
import com.timappweb.timapp.utils.TestUtil;
import com.timappweb.timapp.utils.annotations.AuthState;
import com.timappweb.timapp.utils.annotations.ClearAuth;
import com.timappweb.timapp.utils.annotations.ClearConfig;
import com.timappweb.timapp.utils.annotations.ClearDB;
import com.timappweb.timapp.utils.annotations.ClearDBTable;
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

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

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

    public void beforeTest(){
        Log.i(TAG, "Initialize test...");
        FacebookApiHelper.init();
        this.testAnnoted.execute();
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

    public static class TestAnnotedMatcher{

        private HashMap<Class<? extends Annotation>, Runnable> annotations;

        public TestAnnotedMatcher() {
            this.annotations = new HashMap<>();
        }

        public <T extends Annotation> void addMatcher(Class<T> clazz, Runnable<T> r) {
            this.annotations.put(clazz, r);
        }

        public void execute(Description description){
            Log.i(TAG, "Executing annotations...");
            for (Map.Entry<? extends Class<? extends Annotation>, Runnable> entry: annotations.entrySet()){
                Annotation annotation = description.getAnnotation(entry.getKey());
                if (annotation != null){
                    Log.i(TAG, "Executing annotation: " + entry.getKey().toString());
                    entry.getValue().run(annotation);
                }
            }
            Log.i(TAG, "End executing annotations...");
        }
    }

    public interface Runnable<T extends Annotation>{
        void run(T annotation);
    }


    public class TestAnnotated extends TestWatcher {

        private TestAnnotedMatcher matcher;
        private Description description;

        @Override
        protected void starting( Description description) {
            this.description = description;

            this.matcher = new TestAnnotedMatcher();
            matcher.addMatcher(ClearAuth.class, new Runnable<ClearAuth>() {
                @Override
                public void run(ClearAuth annotation) {
                    MyApplication.logout();
                }
            });


            matcher.addMatcher(ClearFirstStart.class, new Runnable<ClearFirstStart>() {
                @Override
                public void run(ClearFirstStart annotation) {
                    MyApplication.clearStoredData();
                }
            });

            matcher.addMatcher(ClearConfig.class, new Runnable<ClearConfig>() {
                @Override
                public void run(ClearConfig annotation) {
                    ConfigurationProvider.clearAll();
                }
            });

            matcher.addMatcher(ClearDB.class, new Runnable<ClearDB>() {
                @Override
                public void run(ClearDB annotation) {
                    Context context = MyApplication.getApplicationBaseContext();
                    String dbName = context.getString(R.string.db_name);
                    context.deleteDatabase(dbName);
                /*
                Configuration dbConfiguration = new Configuration.Builder(context)
                        .setDatabaseName(dbName)
                        .setDatabaseVersion(12)
                        .addModelClasses(MyClass.class)
                        .create();
                ActiveAndroid.initialize(dbConfiguration);*/
                }
            });

            matcher.addMatcher(CreateConfigAction.class, new Runnable<CreateConfigAction>() {
                @Override
                public void run(CreateConfigAction annotation) {
                    if (annotation.replaceIfExists()) {
                        ConfigurationProvider.clearAll();
                    }
                    if (!ConfigurationProvider.hasFullConfiguration()) {
                        ConfigurationProvider
                                .load(MyApplication.getApplicationBaseContext())
                                .execute();
                    }
                    assertTrue("Cannot load full app configuration from the server: ", ConfigurationProvider.hasFullConfiguration());
                }
            });

            matcher.addMatcher(CreateLastLaunch.class, new Runnable<CreateLastLaunch>() {
                @Override
                public void run(CreateLastLaunch annotation) {
                    MyApplication.updateLastLaunch();
                }

            });

            matcher.addMatcher(CreateAuthAction.class, new Runnable<CreateAuthAction>() {
                @Override
                public void run(CreateAuthAction annotation) {
                    if ( annotation.replaceIfExists()
                            || !MyApplication.isLoggedIn()
                            || (!MyApplication.getAuthManager().isLoggedWithProvider(annotation.providerId(), annotation.payloadId()))) {
                        UsersFixture.init();
                        AuthManager.LoginMethod loginMethod = MyApplication.getAuthManager().getProvider(annotation.providerId());
                        Object loginPayload = UsersFixture.getLoginPayload(annotation.providerId(), annotation.payloadId());
                        Log.i(TAG, "@BeforeTest: Login with payload: " + loginPayload);
                        MyApplication.getAuthManager()
                                .logWith(loginMethod, loginPayload)
                                .onFinally(new HttpCallManager.FinallyCallback() {
                                    @Override
                                    public void onFinally(Response response, Throwable error) {
                                        synchronized (AbstractActivityTest.this) {
                                            AbstractActivityTest.this.notify();
                                        }
                                    }
                                });
                        synchronized (AbstractActivityTest.this) {
                            try {
                                AbstractActivityTest.this.wait();
                            } catch (InterruptedException e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                        assertTrue("User must be logged in", MyApplication.isLoggedIn());
                    }
                }

            });

            matcher.addMatcher(AuthState.class, new Runnable<AuthState>() {
                @Override
                public void run(AuthState annotation) {
                    assertTrue("User must be logged in to perform this test",
                            annotation.logging() != AuthState.LoginState.YES || MyApplication.isLoggedIn());
                    if (annotation.logging() == AuthState.LoginState.NO) {
                        MyApplication.getAuthManager().logout();
                        assertTrue("User must NOT be logged in to perform this test", !MyApplication.isLoggedIn());
                    }
                }
            });

            matcher.addMatcher(ConfigState.class, new Runnable<ConfigState>() {
                @Override
                public void run(ConfigState annotation) {
                    assertTrue("Rules should be loaded in app state to perform this test",
                            ConfigurationProvider.hasRulesConfig() == annotation.rules());
                    assertTrue("Event categories should be loaded in app state to perform this test",
                            ConfigurationProvider.hasEventCategoriesConfig() == annotation.eventCategories());
                    assertTrue("Spot categories should be loaded in app state to perform this test",
                            ConfigurationProvider.hasSpotCategoriesConfig() == annotation.spotCategories());
                }
            });

            matcher.addMatcher(ClearDBTable.class, new Runnable<ClearDBTable>() {
                @Override
                public void run(ClearDBTable annotation) {
                    for (Class<? extends MyModel> model: annotation.models()){
                        SQLite.delete().from(model).execute();
                    }
                }
            });
        }

        public void execute(){
            this.matcher.execute(this.description);
        }

    }
}
