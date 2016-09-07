package com.timappweb.timapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.utils.PictureUtility;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Stephane on 17/08/2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ResizePictureTest {

    private static final String TAG = "UploadPictureTest"
            ;
    @Rule
    public ActivityTestRule<EventActivity> mActivityRule = new ActivityTestRule<>(
            EventActivity.class);

    @Test
    public void testResizeFile() throws IOException {
        //URL url = Thread.currentThread().getContextClassLoader().getResource("pictures/big_light.jpg");
        URL url = this.getClass().getClassLoader().getResource("pictures/big_light.jpg");
        assertNotNull(url);
        File file = new File(url.getPath());
        assertNotNull(file);
        assertTrue(file.exists() && file.isFile());
        Log.i(TAG, "File size before resizing: " + file.length() / (1024*1024) + "MB");
        file = PictureUtility.resize(file, 1000, 1000);
        Log.i(TAG, "File size after resizing: " + file.length() / (1024*1024) + "MB");
    }

    @Test
    public void testResizeBitmap() {
        // Both
        Bitmap bitmap = Bitmap.createBitmap(5000, 4000, Bitmap.Config.ARGB_4444);
        bitmap = PictureUtility.resize(bitmap, 400, 300);
        assertTrue(bitmap.getWidth() == 375);
        assertTrue(bitmap.getHeight() == 300);

        // Only height
        bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_4444);
        bitmap = PictureUtility.resize(bitmap, 400, 300);
        assertTrue(bitmap.getWidth() == 300);
        assertTrue(bitmap.getHeight() == 300);

        // Only width
        bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_4444);
        bitmap = PictureUtility.resize(bitmap, 300, 200);
        assertTrue(bitmap.getWidth() == 300);
        assertTrue(bitmap.getHeight() == 150);
    }
}
