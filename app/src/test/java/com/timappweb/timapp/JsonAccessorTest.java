package com.timappweb.timapp;

import com.google.gson.JsonObject;
import com.timappweb.timapp.utils.JsonAccessor;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Created by Stephane on 12/10/2016.
 */

public class JsonAccessorTest {

    private JsonObject data;
    private JsonAccessor accessor;

    @Before
    public void beforeTest() {
        this.generateFixture();
        this.accessor = new JsonAccessor(this.data);
    }

    @Test
    public void testHas(){
        assertTrue(accessor.has("A"));
        assertFalse(accessor.has("C"));

        assertTrue(accessor.has("B.A"));
        assertFalse(accessor.has("B.Z"));
    }

    @Test
    public void testGet(){
        assertEquals("value.A", accessor.get("A").getAsString());
        assertEquals(null, accessor.get("C"));

        assertEquals("value.B.A", accessor.get("B.A").getAsString());
        assertEquals(null, accessor.get("B.Z"));
    }

    @Test
    public void testGetNotNull(){
        assertEquals("value.A", accessor.get("A").getAsString());

        try{
            accessor.getNotNull("A.Z");
            assertFalse("Should throw an exception", true);
        }
        catch (JsonAccessor.MissingKeyException ex){

        }
    }

    private void generateFixture(){
        data = new JsonObject();
        data.addProperty("A", "value.A");

        JsonObject B = new JsonObject();
        B.addProperty("A", "value.B.A");
        B.addProperty("C", "value.B.C");

        data.add("B", B);
    }
}
