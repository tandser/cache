package ru.tandser.cache;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public abstract class CacheTest extends AbstractTest {

    private static final int    NUM_DATA  = 100;
    private static final String TEST_DATA = "50";

    private Cache<Integer, Object> cache;

    @After
    public void after() {
        cache.clear();
    }

    @Test
    public void put() {
        assertEquals(0, cache.size());

        fillCache();

        assertEquals(NUM_DATA, cache.size());
    }

    @Test
    public void get() {
        int key = TEST_DATA.hashCode();

        assertNull(cache.get(key));

        fillCache();

        String actual = (String) cache.get(key);

        assertEquals(TEST_DATA, actual);
    }

    @Test
    public void remove() {
        int key = TEST_DATA.hashCode();

        assertFalse(cache.contains(key));
        assertNull(cache.get(key));

        fillCache();

        assertTrue(cache.contains(key));
        assertNotNull(cache.get(key));

        cache.remove(key);

        assertFalse(cache.contains(key));
        assertNull(cache.get(key));
    }

    @Test
    public void clear() {
        assertEquals(0, cache.size());

        fillCache();

        assertEquals(NUM_DATA, cache.size());

        cache.clear();

        assertEquals(0, cache.size());
    }

    /* Service methods */

    private void fillCache() {
        for (int i = 0; i < NUM_DATA; i++) {
            String data = Integer.toString(i);
            cache.put(data.hashCode(), data);
        }
    }

    /* Setters and Getters */

    public void setCache(Cache<Integer, Object> cache) {
        this.cache = cache;
    }
}