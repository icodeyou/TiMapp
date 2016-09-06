package com.timappweb.timapp.utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.data.models.EventPost;
import com.timappweb.timapp.rest.io.request.RestQueryParams;
import com.timappweb.timapp.utils.AreaDataCaching.AreaDataLoaderInterface;
import com.timappweb.timapp.utils.AreaDataCaching.AreaRequestHistory;
import com.timappweb.timapp.utils.AreaDataCaching.AreaRequestItemInterface;
import com.timappweb.timapp.utils.AreaDataCaching.CoordinateConverter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by stephane on 9/17/2015.
 * Test to verify data loading
 */
public class AreaDataLoaderTest {

    private TestAreaDataLoader dataLoader;

    @BeforeClass
    public static void setUpClass() {
        System.out.println("@BeforeClass setUpClass");
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        System.out.println("@AfterClass tearDownClass");
    }

    @Before
    public void setUp() {
        System.out.println("@Before setUp");
        dataLoader = new TestAreaDataLoader();
    }

    @After
    public void tearDown() throws IOException {
        System.out.println("@After tearDown");
    }

    @Test
    public void testLoadingAreas(){
        LatLngBounds bounds = new LatLngBounds(new LatLng(1.323232, 1.1123232), new LatLng(3.733232, 2.9822834));
        AreaRequestHistory history = new AreaRequestHistory(new AreaDataLoaderInterface() {
            @Override
            public void load(IntPoint point, AreaRequestItemInterface request, RestQueryParams conditions) {

            }
        });
        history.init(bounds);

        history.DELAY_BEFORE_UPDATE_REQUEST = 0;
        double areaHeight = CoordinateConverter.convert(history.areaHeight);
        double areaWidth = CoordinateConverter.convert(history.areaWidth);

        assertEquals(0, dataLoader.countLoadCall);

        // If we use the same point as initialisation, there is only one area loaded
        history.update(bounds);
        history.update(bounds);
        assertEquals(1, history.areas.size());
        assertEquals(1, dataLoader.countLoadCall);

        // Loading another bound
        LatLng newNortheast = new LatLng(bounds.northeast.latitude + areaHeight,bounds.northeast.longitude + areaWidth);
        LatLngBounds newBounds = new LatLngBounds(bounds.southwest, newNortheast);
        history.update(newBounds);
        history.update(newBounds);
        assertEquals(4, history.areas.size());
        assertEquals(4, dataLoader.countLoadCall);

    }

    /**
     * Testing out dated data
     * @warning If it is run with the debugger there are great chance that the test will fail as
     * we get the machine timestamp
     * @throws InterruptedException
     */
    @Test
    public void testOutDatedAreas() throws InterruptedException {
        LatLngBounds bounds = new LatLngBounds(new LatLng(1.323232, 1.1123232), new LatLng(3.733232, 2.9822834));
        AreaRequestHistory history = new AreaRequestHistory(new AreaDataLoaderInterface() {
            @Override
            public void load(IntPoint point, AreaRequestItemInterface request, RestQueryParams conditions) {

            }
        });
        history.init(bounds);

        history.DELAY_BEFORE_UPDATE_REQUEST = 1;
        history.update(bounds);
        assertEquals(1, history.areas.size());
        assertEquals(1, dataLoader.countLoadCall);
        assertEquals(0, dataLoader.countClearCall);
        history.update(bounds);
        assertEquals(1, history.areas.size());
        assertEquals(1, dataLoader.countLoadCall);
        assertEquals(0, dataLoader.countClearCall);

        // Out dated
        Thread.sleep((history.DELAY_BEFORE_UPDATE_REQUEST * 1000) + 100);
        int lastUpdate = 0;//history.areas.get(new IntPoint(0, 0)).getLastUpdateDelay();
        assertTrue(lastUpdate >= history.DELAY_BEFORE_UPDATE_REQUEST);

        history.update(bounds);
        //lastUpdate = history.areas.get(new IntPoint(0, 0)).getLastUpdateDelay();
        assertTrue(lastUpdate == 0);

        assertEquals(1, history.areas.size());
        assertEquals(2, dataLoader.countLoadCall);
        assertEquals(0, dataLoader.countClearCall);
        history.update(bounds);
        assertEquals(1, history.areas.size());
        assertEquals(2, dataLoader.countLoadCall);
        assertEquals(0, dataLoader.countClearCall);
    }

    @Test
    public void testTooFarArea() throws InterruptedException {
        LatLngBounds bounds = new LatLngBounds(new LatLng(1.323232, 1.1123232), new LatLng(3.733232, 2.9822834));
        AreaRequestHistory history = new AreaRequestHistory(new AreaDataLoaderInterface() {
            @Override
            public void load(IntPoint point, AreaRequestItemInterface request, RestQueryParams conditions) {

            }
        });
        history.init(bounds);

        history.MAXIMUM_ORIGIN_DISTANCE = 2;
        history.DELAY_BEFORE_UPDATE_REQUEST = 10;
        history.update(bounds);
        history.update(bounds);
        assertEquals(1, history.areas.size());
        assertEquals(1, dataLoader.countLoadCall);

        // Very far away bound
        IntLatLngBounds newBounds = history.getBoundFromPoint(new IntPoint(history.MAXIMUM_ORIGIN_DISTANCE + 2, history.MAXIMUM_ORIGIN_DISTANCE + 2));
        history.update(CoordinateConverter.convert(newBounds));
        assertEquals(0, dataLoader.countClearAllCall);
        assertEquals(1, dataLoader.countClearCall);
        history.update(CoordinateConverter.convert(newBounds));
        assertEquals(0, dataLoader.countClearAllCall);
        assertEquals(1, dataLoader.countClearCall);
    }

    class TestAreaDataLoader implements AreaDataLoaderInterface {
        public int countLoadCall = 0;
        public int countClearCall = 0;
        public int countClearAllCall = 0;

        @Override
        public void load(IntPoint point, AreaRequestItemInterface request, RestQueryParams conditions) {
            System.out.println("Loading point " + point);
            List data = new LinkedList<EventPost>();
            data.add(new EventPost());
            request.setData(data);
            countLoadCall++;
        }

    }
}