package com.timappweb.timapp;

import com.timappweb.timapp.utils.SearchHistory;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by stephane on 9/17/2015.
 * Test to verify data loading
 */
public class SearchHistoryTest {

    static List<Tag> data = new LinkedList<>();
    public int counterOnComplete = 0;
    public int counterLoad = 0;


    @BeforeClass
    public static void setUpClass() {
        // c -> 9
        // ca -> 8
        // can -> 3
        // cana -> 1 ..
        data.add(new Tag("canard"));
        data.add(new Tag("cancre"));
        data.add(new Tag("canne"));
        data.add(new Tag("cache"));
        data.add(new Tag("car"));
        data.add(new Tag("carte"));
        data.add(new Tag("came"));
        data.add(new Tag("calle"));
        data.add(new Tag("crache"));

    }
    @Test
    public void testSubTerms() throws InterruptedException {
        int minimumSearchLength = 2;
        int maximumResult = 4;
        final SearchHistory history = new SearchHistory<Tag>(minimumSearchLength, maximumResult);

        SearchHistory.Item itemCa = history.addInCache("ca", new LinkedList(), false);
        SearchHistory.Item itemCan = history.addInCache("can", new LinkedList(), false);

        SearchHistory.Item item = history.getTermOrSubTerm("canard");
        assertNotNull(item);
        assertEquals("Should return the biggest search term", itemCan, item);

        SearchHistory.Item nullItem = history.getTermOrSubTerm("blob");
        assertEquals(null, nullItem);
    }

    @Test
    public void testSearch() throws InterruptedException {
        int minimumSearchLength = 2;
        int maximumResult = 4;
        final SearchHistory history = new SearchHistory<Tag>(minimumSearchLength, maximumResult);
        history.setDataProvider(new SearchHistory.DataProvider() {

                    @Override
                    public void load(String term) {
                        counterLoad += 1;
                        new Thread(new LoadDataThread(term, history)).start();
                    }
                    @Override
                    public void onLoadEnds() {

                    }

                    @Override
                    public void onSearchComplete(String term, List data) {
                        System.out.println("Search complete for term " + term);
                        counterOnComplete++;
                        synchronized (SearchHistoryTest.this){
                            SearchHistoryTest.this.notify();
                        }
                    }
                });

        // Test that we use cache when there is less that maximumResult data
        history.search("c");
        assertEquals("Search term is too short, should NOT trigger a load", 0, counterLoad);
        assertEquals(0, counterOnComplete);
        history.search("ca");
        assertEquals("Should trigger a load", 1, counterLoad);
        assertEquals(0, counterOnComplete);
        history.search("can");
        history.search("can");
        history.search("can");
        assertEquals("Searching for the same term multiple times should NOT trigger multiple load", 2, counterLoad);
        assertEquals(0, counterOnComplete);

        synchronized (this){
            while (counterOnComplete < 2) this.wait();
        }

        assertEquals(2, counterOnComplete);

        history.search("cana");
        assertEquals("Increasing search term for an already completed search should NOT trigger a load", 2, counterLoad);
        assertEquals(3, counterOnComplete);
        history.search("canar");
        assertEquals("Increasing search term for an already completed search should NOT trigger a load", 2, counterLoad);
        assertEquals(4, counterOnComplete);
        history.search("canard");
        assertEquals(2, counterLoad);
        assertEquals(5, counterOnComplete);

        Thread.sleep(2000);
    }

    public class LoadDataThread implements Runnable {


        private final String term;
        private final SearchHistory history;

        public LoadDataThread(String term, SearchHistory history) {
            this.term = term;
            this.history = history;
        }

        public void run() {
            try {
                System.out.println("Starting thread with term: " + term);
                Thread.sleep(500);
                history.onSearchResponse(this.term, SearchHistory.filterData(data, term), false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    private static class Tag implements SearchHistory.SearchableItem{
        private final String name;

        public Tag(String tag) {
            this.name = tag;
        }

        @Override
        public boolean matchSearch(String term) {
            return this.name.startsWith(term);
        }
    }

}