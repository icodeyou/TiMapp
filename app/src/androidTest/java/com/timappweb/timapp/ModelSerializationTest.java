package com.timappweb.timapp;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.activeandroid.Model;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SpotCategory;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.dummy.DummySpotFactory;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.utils.SerializeHelper;

import org.junit.Test;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ModelSerializationTest extends ApplicationTestCase<Application> {


    public ModelSerializationTest() {
        super(Application.class);
    }

    @Test
    public void testSerializeSimpleClass() {
        serializeDeserialize(new SimpleClass(), SimpleClass.class);
    }

    @Test
    public void testSerializeDBModel() throws CannotSaveModelException {
        serializeDeserializeModel(new Spot(), Spot.class);

        // Test Spot saved:
        Spot spot = DummySpotFactory.create();
        spot = (Spot) spot.mySave();
        serializeDeserializeModel(spot, Spot.class);

        // Clean:
        spot.delete();
    }

    protected <T extends SyncBaseModel> void serializeDeserializeModel(T input, Class<T> clazz) {
        String data = SerializeHelper.packModel(input, clazz);
        T output = SerializeHelper.unpackModel(data, clazz);
        assertNotNull(output);
        assertEquals(input.getRemoteId(), output.getRemoteId());
    }

    protected <T> void serializeDeserialize(T input, Class<T> clazz){
        String data = SerializeHelper.pack(input);
        T output = SerializeHelper.unpack(data, clazz);
        assertNotNull(output);
        assertEquals(input, output);
    }

    private class SimpleClass{
        @Expose
        public int a = 1;
        @Expose
        public String b = "This is me";
        @Expose
        private long c = 23L;
        @Expose
        protected double d = 234;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SimpleClass that = (SimpleClass) o;

            if (a != that.a) return false;
            if (c != that.c) return false;
            if (Double.compare(that.d, d) != 0) return false;
            return b != null ? b.equals(that.b) : that.b == null;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = a;
            result = 31 * result + (b != null ? b.hashCode() : 0);
            result = 31 * result + (int) (c ^ (c >>> 32));
            temp = Double.doubleToLongBits(d);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }
}