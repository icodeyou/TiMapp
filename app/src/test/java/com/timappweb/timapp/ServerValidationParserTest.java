package com.timappweb.timapp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.timappweb.timapp.rest.io.responses.ServerValidationParser;
import com.timappweb.timapp.utils.JsonAccessor;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
/**
 * Created by Stephane on 12/10/2016.
 */

public class ServerValidationParserTest {

    private JsonObject data;
    private ServerValidationParser parser;

    @Before
    public void beforeTest() {
        this.generateFixture();
        this.parser = new ServerValidationParser(this.data);
    }

    @Test
    public void testMessage(){
        assertEquals("Places.description.feedback.1", this.parser.getMessage("Places.description"));
        assertEquals("Places.name.feedback.1\nPlaces.name.feedback.2", this.parser.getMessage("Places.name"));
        assertEquals("Places.content.feedback", this.parser.getMessage("Places.content"));
        assertEquals("Places.content.feedback", this.parser.getMessage("Places.content"));
        assertEquals("12", this.parser.getMessage("Places.int"));
    }

    private void generateFixture(){
        String msg = "" +
                "{" +
                "   \"Places\": {\n" +
                "       \"int\": 12," +
                "       \"name\": [\n" +
                "           \"Places.name.feedback.1\",\n" +
                "           \"Places.name.feedback.2\"\n" +
                "       ],\n" +
                "       \"description\": [\n" +
                "           \"Places.description.feedback.1\"\n" +
                "       ],\n" +
                "       \"content\":  \"Places.content.feedback\"\n" +
                "   }" +
                "}";
        //System.out.println(msg);
        this.data = new JsonParser().parse(msg).getAsJsonObject();
    }
}
