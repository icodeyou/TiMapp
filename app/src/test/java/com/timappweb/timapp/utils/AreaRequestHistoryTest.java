package com.timappweb.timapp.utils;

import com.timappweb.timapp.entities.Post;

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
    }

    @Test
    public void testGetID() throws Exception {
        IntLatLng center = new IntLatLng(12,13);
        AreaRequestHistory history = new AreaRequestHistory(1,2, center);

        int width10 = history.AREA_WIDTH / 10;
        int height10 = history.AREA_HEIGHT / 10;

        // The center has 0,0 coordinate
        IntPoint p = history.getIntPoint(center);
        assertEquals(p, new IntPoint(0,0));

        // A point in the center has 0,0 coordinate
        p = history.getIntPoint(new IntLatLng(center.latitude + height10, center.longitude + width10));
        assertEquals(p, new IntPoint(0,0));

        p = history.getIntPoint(new IntLatLng(center.latitude + history.AREA_HEIGHT, center.longitude + history.AREA_WIDTH));
        assertEquals(p, new IntPoint(1,1));
        p = history.getIntPoint(new IntLatLng(center.latitude, center.longitude + history.AREA_WIDTH));
        assertEquals(p, new IntPoint(0,1));
        p = history.getIntPoint(new IntLatLng(center.latitude + history.AREA_HEIGHT, center.longitude));
        assertEquals(p, new IntPoint(1,0));

        p = history.getIntPoint(new IntLatLng(center.latitude, center.longitude + history.AREA_WIDTH + width10));
        assertEquals(p, new IntPoint(0,1));
        p = history.getIntPoint(new IntLatLng(center.latitude + history.AREA_HEIGHT + height10, center.longitude));
        assertEquals(p, new IntPoint(1,0));

        // Negatives offset
        p = history.getIntPoint(new IntLatLng(center.latitude, center.longitude - history.AREA_WIDTH - width10));
        assertEquals(new IntPoint(0,-1), p);
        p = history.getIntPoint(new IntLatLng(center.latitude - history.AREA_HEIGHT - height10, center.longitude));
        assertEquals(new IntPoint(-1, 0), p);

    }

    @Test
    public void testGetBoundFromPoint() throws Exception {
        IntLatLng center = new IntLatLng(12,-13);
        AreaRequestHistory history = new AreaRequestHistory(1,2, center);

        IntPoint pCenter = new IntPoint(0,0);

        IntLatLngBounds bounds = history.getBoundFromPoint(pCenter);
        assertEquals(center, bounds.southwest);

        assertEquals(new IntLatLng(center.latitude + history.AREA_HEIGHT, center.longitude + history.AREA_WIDTH), bounds.northeast);
    }

    @Test
    public void testBountToPointToBound() throws Exception {
        IntLatLng center = new IntLatLng(-1213, 233247);
        AreaRequestHistory history = new AreaRequestHistory(2,1, center);

        for (int y = -10; y <= 10; y++){
            IntPoint p = new IntPoint(y,0);
            IntLatLngBounds bounds = history.getBoundFromPoint(p);
            assertEquals(p, history.getIntPoint(bounds.southwest));
        }

        for (int y = -10; y < 10; y += 1){
            for (int x = -10; x < 10; x += 1){
                IntPoint p = new IntPoint(y,x);
                IntLatLngBounds bounds = history.getBoundFromPoint(p);
                assertEquals(p, history.getIntPoint(bounds.southwest));
            }
        }

    }

    @Test
    public void hasInCache() throws Exception {
        IntLatLng center = new IntLatLng(-100, 3000);
        AreaRequestHistory history = new AreaRequestHistory(2000,4000, center);

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
        AreaRequestHistory history = new AreaRequestHistory(30,40, center);

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