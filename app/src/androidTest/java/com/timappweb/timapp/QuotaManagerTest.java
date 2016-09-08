package com.timappweb.timapp;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.models.UserQuota;

import org.junit.Test;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class QuotaManagerTest extends ApplicationTestCase<Application> {

    private static final String TAG = "QuotaManagerTest";

    public QuotaManagerTest() {
        super(Application.class);
    }

    @Test
    public void testAddQuota(){
        UserQuota quota = QuotaManager.instance().getQuota(QuotaType.ADD_EVENT);
        quota.resetCounts();
        assertTrue(quota.hasValidQuotas());

        // Check last activity
        quota.increment();
        assertFalse(quota.hasValidQuotas());

        /*
        // Check quota minute
        quota.resetLastActivity();
        quota.setQuotaMinute(1);
        quota.increment();
        assertFalse(quota.assertValidQuotas());
        quota.setQuotaMinute(0);

        // Check quota hour
        quota.resetLastActivity();
        quota.setQuotaHour(1);
        quota.increment();
        assertFalse(quota.assertValidQuotas());
        quota.setQuotaHour(0);*/

    }
    @Test
    public void testShowQuota(){
        showQuota(QuotaType.ADD_EVENT);
        showQuota(QuotaType.ADD_PICTURE);
        showQuota(QuotaType.ADD_TAGS);
        showQuota(QuotaType.INVITE_FRIEND);
        showQuota(QuotaType.NOTIFY_STATUS);
    }

    private void showQuota(int type){
        UserQuota quota = QuotaManager.instance().getQuota(type);
        Log.i(TAG, quota != null ? quota.toString() : "NO QUOTA FOR TYPE: " + type);
    }
}