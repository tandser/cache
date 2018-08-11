package ru.tandser.cache;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class TwoLevelCacheTest extends AbstractTest {

    private static final int NUM_DATA = 25;

    private static Map<Integer, Integer> gettingCache;
    private static Set<Integer>          keysL1CacheShift1;
    private static Set<Integer>          keysL2CacheShift1;
    private static Set<Integer>          keysL1CacheShift2;
    private static Set<Integer>          keysL2CacheShift2;

    @Autowired private TwoLevelCache cache;

    @BeforeClass
    public static void beforeClass() {
        gettingCache = TestUtils.gettingCache();
        keysL1CacheShift1 = TestUtils.keysL1CacheShift1();
        keysL2CacheShift1 = TestUtils.keysL2CacheShift1();
        keysL1CacheShift2 = TestUtils.keysL1CacheShift2();
        keysL2CacheShift2 = TestUtils.keysL2CacheShift2();
    }

    @After
    public void after() {
        cache.clear();
    }

    @Test
    public void put() throws Exception {
        assertEquals(0, cache.size());

        fillCache(0, NUM_DATA);

        assertEquals(NUM_DATA, cache.size());
        assertEquals(NUM_DATA, cache.getL1Cache().size());
        assertEquals(0,        cache.getL2Cache().size());

        TimeUnit.SECONDS.sleep(60);

        callCache();

        TimeUnit.SECONDS.sleep(60);

        assertEquals(NUM_DATA, cache.size());
        assertEquals(14,       cache.getL1Cache().size());
        assertEquals(11,       cache.getL2Cache().size());

        for (Map.Entry<Integer, CacheEntity<?>> cacheEntry : cache.getL1Cache().getCacheMap().entrySet()) {
            assertTrue(cache.contains(cacheEntry.getKey()));
            assertFalse(cache.getL2Cache().contains(cacheEntry.getKey()));
        }

        for (Map.Entry<Integer, CacheEntity<?>> cacheEntry : cache.getL2Cache().getCacheMap().entrySet()) {
            assertTrue(cache.contains(cacheEntry.getKey()));
            assertFalse(cache.getL1Cache().contains(cacheEntry.getKey()));
        }

        callCache();

        TimeUnit.SECONDS.sleep(10);

        assertEquals(NUM_DATA, cache.size());
        assertEquals(11,       cache.getL1Cache().size());
        assertEquals(14,       cache.getL2Cache().size());

        for (Integer key : keysL1CacheShift2) {
            assertTrue(cache.getL1Cache().contains(Integer.toString(key).hashCode()));
        }

        for (Integer key : keysL2CacheShift2) {
            assertTrue(cache.getL2Cache().contains(Integer.toString(key).hashCode()));
        }
    }

    @Test
    public void get() throws Exception {
        fillCache(0, NUM_DATA);

        TimeUnit.SECONDS.sleep(60);

        callCache();

        TimeUnit.SECONDS.sleep(10);

        for (Integer key : keysL1CacheShift1) {
            assertTrue(cache.contains(Integer.toString(key).hashCode()));
            assertTrue(cache.getL1Cache().contains(Integer.toString(key).hashCode()));
        }

        for (Integer key : keysL2CacheShift1) {
            assertTrue(cache.contains(Integer.toString(key).hashCode()));
            assertTrue(cache.getL2Cache().contains(Integer.toString(key).hashCode()));
        }

        String mockFromL1Cache = Integer.toString(keysL1CacheShift1.iterator().next());
        String mockFromL2Cache = Integer.toString(keysL2CacheShift1.iterator().next());

        Integer keyMockFromL1Cache = mockFromL1Cache.hashCode();
        Integer keyMockFromL2Cache = mockFromL2Cache.hashCode();

        assertEquals(cache.get(keyMockFromL1Cache), mockFromL1Cache);
        assertEquals(cache.get(keyMockFromL2Cache), mockFromL2Cache);

        assertEquals(cache.getL1Cache().get(keyMockFromL1Cache), mockFromL1Cache);
        assertEquals(cache.getL2Cache().get(keyMockFromL2Cache), mockFromL2Cache);
    }

    @Test
    public void remove() throws Exception {
        fillCache(0, NUM_DATA);

        TimeUnit.SECONDS.sleep(60);

        callCache();

        TimeUnit.SECONDS.sleep(10);

        Integer keyMockFromL1Cache = Integer.toString(keysL1CacheShift1.iterator().next()).hashCode();
        Integer keyMockFromL2Cache = Integer.toString(keysL2CacheShift1.iterator().next()).hashCode();

        assertTrue(cache.contains(keyMockFromL1Cache));
        assertTrue(cache.contains(keyMockFromL2Cache));

        assertTrue(cache.getL1Cache().contains(keyMockFromL1Cache));
        assertTrue(cache.getL2Cache().contains(keyMockFromL2Cache));

        cache.remove(keyMockFromL1Cache);
        cache.remove(keyMockFromL2Cache);

        assertFalse(cache.contains(keyMockFromL1Cache));
        assertFalse(cache.contains(keyMockFromL2Cache));

        assertFalse(cache.getL1Cache().contains(keyMockFromL1Cache));
        assertFalse(cache.getL2Cache().contains(keyMockFromL2Cache));
    }

    @Test
    public void clear() throws Exception {
        assertEquals(0, cache.size());

        fillCache(0, NUM_DATA);

        TimeUnit.SECONDS.sleep(60);

        callCache();

        TimeUnit.SECONDS.sleep(10);

        assertEquals(NUM_DATA, cache.size());
        assertEquals(14,       cache.getL1Cache().size());
        assertEquals(11,       cache.getL2Cache().size());

        cache.clear();

        assertEquals(0, cache.size());
        assertEquals(0, cache.getL1Cache().size());
        assertEquals(0, cache.getL2Cache().size());
    }

    /* Service methods */

    private void fillCache(int min, int max) {
        for (int i = min; i < max; i++) {
            String data = Integer.toString(i);
            cache.put(data.hashCode(), data);
        }
    }

    private void callCache() {
        for (Map.Entry<Integer, Integer> entry : gettingCache.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                cache.get(Integer.toString(entry.getKey()).hashCode());
            }
        }
    }
}