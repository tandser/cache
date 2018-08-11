package ru.tandser.cache;

import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of the first level cache. The cache is located in
 * RAM. Intended also for multi-threaded use.
 *
 * @author Andrew Timokhin
 */
public class L1Cache implements Cache<Integer, Object> {

    private static final Logger logger = LoggerFactory.getLogger(L1Cache.class);

    private Map<Integer, CacheEntity<Object>> cache;

    {
        cache = new ConcurrentHashMap<>();
    }

    @Override
    public Set<CacheEntity<?>> getCacheSet() {
        return new HashSet<>(cache.values());
    }

    @Override
    public Map<Integer, CacheEntity<?>> getCacheMap() {
        return new HashMap<>(cache);
    }

    @Override
    public void put(Integer key, Object value) {
        logger.debug("put: key = {}, value = {}", key, value);
        cache.put(requireNonNull(key), new CacheEntity<>(requireNonNull(value)));
    }

    @Override
    public Object get(Integer key) {
        Object result = null;

        if (cache.containsKey(requireNonNull(key))) {
            CacheEntity<Object> cacheEntity = cache.get(key);
            cacheEntity.incrementCount();
            result = cacheEntity.getValue();
        }

        logger.debug("get: key = {}, result = {}", key, result);

        return result;
    }

    @Override
    public void remove(Integer key) {
        logger.debug("remove: key = {}", key);

        if (cache.containsKey(requireNonNull(key))) {
            cache.remove(key);
        }
    }

    @Override
    public boolean contains(Integer key) {
        logger.debug("contains: key = {}", key);
        return cache.containsKey(requireNonNull(key));
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("size",  cache.size())
                .add("cache", cache)
                .toString();
    }
}