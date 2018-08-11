package ru.tandser.cache.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tandser.cache.Cache;
import ru.tandser.cache.utils.CacheUtils;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;

/**
 * Implementing the logic for partially clearing the cache when it
 * is full. The cache is cleared to the number of elements equal to
 * <i>cacheCapacity</i> * <i>cacheLoadFactor</i>.
 *
 * @author Andrew Timokhin
 */
public class CacheEraserImpl implements CacheEraser<Integer, Object> {

    private static final Logger logger = LoggerFactory.getLogger(CacheEraserImpl.class);

    @Override
    public void erase(Cache<Integer, Object> cache, int cacheCapacity, double cacheLoadFactor) {
        logger.debug("erase: cache = {}, cacheCapacity = {}, cacheLoadFactor = {}", cache, cacheCapacity, cacheLoadFactor);

        int currentCacheSize = cache.size();

        if (currentCacheSize >= cacheCapacity) {
            int toRemove = (int) Math.round(currentCacheSize - (cacheCapacity * cacheLoadFactor));

            SortedSet<Map.Entry<Integer, LocalDateTime>> setWithDateTimeCache = CacheUtils.entriesSortedByValues(CacheUtils.getMapWithDateTimeCache(cache));

            Iterator<Map.Entry<Integer, LocalDateTime>> iterator = setWithDateTimeCache.iterator();

            for (int i = 0; i < toRemove; i++) {
                if (iterator.hasNext()) {
                    cache.remove(iterator.next().getKey());
                }
            }
        }
    }
}