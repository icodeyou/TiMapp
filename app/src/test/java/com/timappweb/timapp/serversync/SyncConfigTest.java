package com.timappweb.timapp.serversync;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;

/**
 * Created by stephane on 4/22/2016.
 */
public class SyncConfigTest {

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
    }

    @After
    public void tearDown() throws IOException {
        System.out.println("@After tearDown");
    }
}
