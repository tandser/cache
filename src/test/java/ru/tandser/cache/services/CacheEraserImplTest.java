package ru.tandser.cache.services;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.tandser.cache.AbstractTest;
import ru.tandser.cache.Cache;
import ru.tandser.cache.utils.CacheUtils;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CacheEraserImplTest extends AbstractTest {

    private static final int    NUM_DATA          = 150;
    private static final int    CACHE_CAPACITY    = 100;
    private static final double CACHE_LOAD_FACTOR = 0.75f;

    @Autowired private Cache<Integer, Object>       l1Cache;
    @Autowired private Cache<Integer, Object>       l2Cache;
    @Autowired private CacheEraser<Integer, Object> cacheEraser;

    @Before
    public void before() {
        for (int i = 0; i < NUM_DATA; i++) {
            String data = Integer.toString(i);

            l1Cache.put(data.hashCode(), data);
            l2Cache.put(data.hashCode(), data);
        }
    }

    @After
    public void after() {
        l1Cache.clear();
        l2Cache.clear();
    }

    @Test
    public void erase() {
        SortedSet<Map.Entry<Integer, LocalDateTime>> setWithDateTimeL1Cache = CacheUtils.entriesSortedByValues(CacheUtils.getMapWithDateTimeCache(l1Cache));
        SortedSet<Map.Entry<Integer, LocalDateTime>> setWithDateTimeL2Cache = CacheUtils.entriesSortedByValues(CacheUtils.getMapWithDateTimeCache(l1Cache));

        cacheEraser.erase(l1Cache, CACHE_CAPACITY, CACHE_LOAD_FACTOR);
        cacheEraser.erase(l2Cache, CACHE_CAPACITY, CACHE_LOAD_FACTOR);

        int targetNum = (int) Math.round(NUM_DATA - (CACHE_CAPACITY * CACHE_LOAD_FACTOR));

        Iterator<Map.Entry<Integer, LocalDateTime>> l1CacheIterator = setWithDateTimeL1Cache.iterator();
        Iterator<Map.Entry<Integer, LocalDateTime>> l2CacheIterator = setWithDateTimeL2Cache.iterator();

        for (int i = 0; i < NUM_DATA; i++) {
            if (i < targetNum) {
                if (l1CacheIterator.hasNext()) {
                    assertFalse(l1Cache.contains(l1CacheIterator.next().getKey()));
                }

                if (l2CacheIterator.hasNext()) {
                    assertFalse(l2Cache.contains(l2CacheIterator.next().getKey()));
                }
            } else {
                if (l1CacheIterator.hasNext()) {
                    assertTrue(l1Cache.contains(l1CacheIterator.next().getKey()));
                }

                if (l2CacheIterator.hasNext()) {
                    assertTrue(l2Cache.contains(l2CacheIterator.next().getKey()));
                }
            }
        }
    }
}