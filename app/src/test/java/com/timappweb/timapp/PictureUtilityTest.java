package com.timappweb.timapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.squareup.picasso.Picasso;
import com.timappweb.timapp.utils.PictureUtility;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;

/**
 * Created by Stephane on 31/08/2016.
 */
public class PictureUtilityTest {

    @BeforeClass
    public static void setUpClass() {
        System.out.println("@BeforeClass setUpClass");
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        System.out.println("@AfterClass tearDownClass");
    }

    @Test
    public void testResizeBiggerPicture() throws IOException, URISyntaxException {
        URL url = this.getClass().getClassLoader().getResource("pictures/big_heavy.jpg");
        File file =  new File(url.toURI());
        PictureUtility.resize(file, 200, 200);
    }

    @Test
    public void testResizeBitmap() throws IOException, URISyntaxException {
        URL url = this.getClass().getClassLoader().getResource("pictures/big_light.jpg");

        File f = new File(url.getPath());
        assertTrue(f.exists() && !f.isDirectory());
        System.out.println("File size "+f.getAbsolutePath()+ ": " + f.length() / 1024.0 + "KB");

        BitmapFactory.Options options = new BitmapFactory.Options ();
        options.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options);

        bitmap = Bitmap.createBitmap(100, 300, Bitmap.Config.ARGB_4444);
        PictureUtility.resize(bitmap, 200, 200);

        assertEquals(bitmap.getWidth(), 200);
        assertEquals(bitmap.getHeight(), 200);
    }
}
