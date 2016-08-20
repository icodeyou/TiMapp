package com.timappweb.timapp.utils;

import com.timappweb.timapp.data.models.Tag;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
    public void testSearch() throws InterruptedException {

        final SearchHistory history = new SearchHistory<Tag>(2, 4);
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
            public SearchHistory.Item onSearchComplete(String term, List data) {
                System.out.println("Search complete for term " + term);
                counterOnComplete++;
            }
        });

        // Test that we use cache when there is less that maximumResult data
        history.search("c");
        assertEquals(0, counterLoad);
        assertEquals(0, counterOnComplete);
        history.search("ca");
        assertEquals(1, counterLoad);
        assertEquals(0, counterOnComplete);
        history.search("can");
        history.search("can");
        history.search("can");
        assertEquals(2, counterLoad);
        assertEquals(0, counterOnComplete);

        Thread.sleep(1000);

        history.search("cana");
        assertEquals(2, counterLoad);
        assertEquals(3, counterOnComplete);
        history.search("canar");
        assertEquals(2, counterLoad);
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
                history.onSearchResponse(this.term, SearchHistory.filterData(data, term));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}