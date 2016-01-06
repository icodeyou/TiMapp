package com.timappweb.timapp.utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.utils.AreaDataCaching.AreaIterator;
import com.timappweb.timapp.utils.AreaDataCaching.AreaRequestHistory;
import com.timappweb.timapp.utils.AreaDataCaching.AreaRequestItem;

import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by stephane on 9/17/2015.
 */
public class AreaRequestHistoryTest {

    @Test
    public void testPoint(){
        assertEquals(new IntPoint(0,0), new IntPoint(0,0));
        assertEquals(new IntPoint(3,-27), new IntPoint(3,-27));
        assertNotEquals(new IntPoint(3, 2), new IntPoint(0, 0));
        assertNotEquals(new IntPoint(1, 0), new IntPoint(0, 0));
        assertNotEquals(new IntPoint(0, 1), new IntPoint(0, 0));
    }

    @Test
    public void testGetID() throws Exception {
        LatLngBounds bounds = new LatLngBounds(new LatLng(12, 13), new LatLng(16, 20));
        AreaRequestHistory history = new AreaRequestHistory(bounds, null);
        IntLatLng center = history.getCenter();


        int width10 = history.areaWidth / 10;
        int height10 = history.areaHeight / 10;

        // The center has 0,0 coordinate
        IntPoint p = history.getIntPoint(center);
        assertEquals(p, new IntPoint(0,0));

        // A point in the center has 0,0 coordinate
        p = history.getIntPoint(new IntLatLng(center.latitude + height10, center.longitude + width10));
        assertEquals(p, new IntPoint(0,0));

        p = history.getIntPoint(new IntLatLng(center.latitude + history.areaHeight, center.longitude + history.areaWidth));
        assertEquals(p, new IntPoint(1,1));
        p = history.getIntPoint(new IntLatLng(center.latitude, center.longitude + history.areaWidth));
        assertEquals(p, new IntPoint(0,1));
        p = history.getIntPoint(new IntLatLng(center.latitude + history.areaHeight, center.longitude));
        assertEquals(p, new IntPoint(1,0));

        p = history.getIntPoint(new IntLatLng(center.latitude, center.longitude + history.areaWidth + width10));
        assertEquals(p, new IntPoint(0,1));
        p = history.getIntPoint(new IntLatLng(center.latitude + history.areaHeight + height10, center.longitude));
        assertEquals(p, new IntPoint(1,0));

        // Negatives offset
        p = history.getIntPoint(new IntLatLng(center.latitude, center.longitude - history.areaWidth - width10));
        assertEquals(new IntPoint(0,-1), p);
        p = history.getIntPoint(new IntLatLng(center.latitude - history.areaHeight - height10, center.longitude));
        assertEquals(new IntPoint(-1, 0), p);

    }

    @Test
    public void testBountToPointToBound() throws Exception {
        LatLngBounds bounds = new LatLngBounds(new LatLng(-1.213, 2.33247), new LatLng(-1.210, 2.53247));
        AreaRequestHistory history = new AreaRequestHistory(bounds, null);

        for (int y = -10; y <= 10; y++){
            IntPoint p = new IntPoint(y,0);
            IntLatLngBounds intBounds = history.getBoundFromPoint(p);
            assertEquals(p, history.getIntPoint(intBounds.southwest));
        }

        for (int y = -10; y < 10; y += 1){
            for (int x = -10; x < 10; x += 1){
                IntPoint p = new IntPoint(y,x);
                IntLatLngBounds intBounds = history.getBoundFromPoint(p);
                assertEquals(p, history.getIntPoint(intBounds.southwest));
            }
        }

    }

    @Test
    public void hasInCache() throws Exception {
        LatLngBounds bounds = new LatLngBounds(new LatLng(-1, 3), new LatLng(2, 7));
        AreaRequestHistory history = new AreaRequestHistory(bounds, null);

        LinkedList<Post> data = new LinkedList<>();
        IntPoint p1 = new IntPoint(2, 4);
        history.areas.put(p1, new AreaRequestItem(10, data));
        assertEquals(true, history.areas.containsKey(p1));

        IntPoint p2 = new IntPoint(2, 4);
        assertEquals(true, history.areas.containsKey(p2));
    }

    @Test
    public void testAreaIterator(){
        IntLatLng center = new IntLatLng(0, 0);
        LatLngBounds bounds = new LatLngBounds(new LatLng(0, 0), new LatLng(30, 40));
        AreaRequestHistory history = new AreaRequestHistory(bounds, null);

        IntPoint northeast = history.getIntPoint(new IntLatLng(center.latitude+1, center.longitude+1));
        IntPoint southwest = history.getIntPoint(center);
        AreaIterator areaIterator = new AreaIterator(southwest, northeast);
        assertEquals(1, areaIterator.size());
        assertEquals(new IntPoint(0,0), areaIterator.next());

        while (areaIterator.hasNext()){
            IntPoint p = areaIterator.next();
            assertNotEquals(null, p);
        }

        // Test element line
        int nbLine = 3;
        southwest = new IntPoint(15, 15);
        northeast = new IntPoint(nbLine + southwest.y, southwest.x);
        areaIterator = new AreaIterator(new IntPoint(southwest), new IntPoint(northeast));
        assertEquals(nbLine + 1, areaIterator.sizeHeight());
        assertEquals(1, areaIterator.sizeWidth());
        assertEquals(new IntPoint(southwest.y + nbLine, southwest.x), northeast);
        int line = 0;
        while (areaIterator.hasNext()){
            IntPoint p = areaIterator.next();
            assertEquals(new IntPoint(southwest.y + line, southwest.x), p);
            line++;
        }
        // Test element colomns
        int nbCol = 3;
        southwest = new IntPoint(15, 15);
        northeast = new IntPoint(southwest.y, nbLine + southwest.x);
        areaIterator = new AreaIterator(new IntPoint(southwest), new IntPoint(northeast));
        assertEquals(1, areaIterator.sizeHeight());
        assertEquals(nbCol+1, areaIterator.sizeWidth());
        assertEquals(nbCol+1, areaIterator.size());
        int col = 0;
        while (areaIterator.hasNext()){
            IntPoint p = areaIterator.next();
            assertEquals(new IntPoint(southwest.y, southwest.x + col++), p);
        }
        // Test element columns
        nbCol = 4;
        nbLine = 3;
        southwest = new IntPoint(15, 15);
        northeast = new IntPoint(southwest.y + nbLine, nbCol + southwest.x);
        areaIterator = new AreaIterator(new IntPoint(southwest), new IntPoint(northeast));
        assertEquals(nbLine+1, areaIterator.sizeHeight());
        assertEquals(nbCol+1, areaIterator.sizeWidth());

        // Test each element
        southwest = new IntPoint(0,0);
        northeast = new IntPoint(1,1);
        areaIterator = new AreaIterator(southwest, northeast);
        assertEquals(4, areaIterator.size());
        IntPoint p = areaIterator.next();
        assertEquals(new IntPoint(0,0), p);
        p = areaIterator.next();
        assertEquals(new IntPoint(0,1), p);
        p = areaIterator.next();
        assertEquals(new IntPoint(1,0), p);
        p = areaIterator.next();
        assertEquals(new IntPoint(1,1), p);
        assertEquals(null, areaIterator.next());

        // Test each element
        southwest = new IntPoint(0,0);
        northeast = new IntPoint(0,1);
        areaIterator = new AreaIterator(southwest, northeast);
        assertEquals(2, areaIterator.size());
        p = areaIterator.next();
        assertEquals(new IntPoint(0,0), p);
        p = areaIterator.next();
        assertEquals(new IntPoint(0,1), p);

        // Test each element
        southwest = new IntPoint(0,0);
        northeast = new IntPoint(1,0);
        areaIterator = new AreaIterator(southwest, northeast);
        assertEquals(2, areaIterator.size());
        p = areaIterator.next();
        assertEquals(new IntPoint(0,0), p);
        p = areaIterator.next();
        assertEquals(new IntPoint(1,0), p);

        // Test each element
        southwest = new IntPoint(0,0);
        northeast = new IntPoint(0,0);
        areaIterator = new AreaIterator(southwest, northeast);
        assertEquals(1, areaIterator.size());
        assertEquals(new IntPoint(0,0), areaIterator.next());
    }


    @Test
    public void testDistancePoint(){
        IntPoint p1 = new IntPoint(3, 7);
        IntPoint p2 = new IntPoint(13, 7);
        assertEquals(10, p1.distance(p2));
        assertEquals(10, p2.distance(p1));

        IntPoint p3 = new IntPoint(13, 27);
        assertEquals(20, p3.distance(p1));
        assertEquals(20, p1.distance(p3));
    }
}