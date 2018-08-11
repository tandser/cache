package ru.tandser.cache;

import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tandser.cache.services.CacheSerializator;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of the second level cache. The cache is located
 * in the file system and less productive than the first level
 * cache. Intended also for multi-threaded use.
 *
 * @author Andrew Timokhin
 */
public class L2Cache implements Cache<Integer, Object> {

    private static final Logger logger = LoggerFactory.getLogger(L2Cache.class);

    private Map<Integer, CacheEntity<Path>> cache;
    private CacheSerializator<Object>       cacheSerializator;

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
        requireNonNull(key);
        requireNonNull(value);

        Path path;

        try {
            path = cacheSerializator.write(value);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }

        logger.debug("put: key = {}, value = {}, path = {}", key, value, path);

        cache.put(key, new CacheEntity<>(path));
    }

    @Override
    public Object get(Integer key) {
        Object result = null;

        if (cache.containsKey(requireNonNull(key))) {
            CacheEntity<Path> cacheEntity = cache.get(key);

            cacheEntity.incrementCount();

            try {
                result = cacheSerializator.read(cacheEntity.getValue());
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }
        }

        logger.debug("get: key = {}, result = {}", key, result);

        return result;
    }

    @Override
    public void remove(Integer key) {
        if (cache.containsKey(requireNonNull(key))) {
            CacheEntity<Path> cacheEntity = cache.get(key);

            logger.debug("remove: key = {}, cacheEntity = {}", key, cacheEntity);

            removeCacheFile(cacheEntity.getValue());

            cache.remove(key);
        }
    }

    @Override
    public boolean contains(Integer key) {
        return cache.containsKey(requireNonNull(key));
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public void clear() {
        for (Map.Entry<Integer, CacheEntity<Path>> item : cache.entrySet()) {
            CacheEntity<Path> cacheEntity = item.getValue();

            logger.debug("clear: cacheEntity = {}", cacheEntity);

            if (cacheEntity != null) {
                removeCacheFile(cacheEntity.getValue());
            }
        }

        cache.clear();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("size",  cache.size())
                .add("cache", cache)
                .toString();
    }

    /* Service methods */

    private void removeCacheFile(Path path) {
        try {
            cacheSerializator.remove(requireNonNull(path));
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    /* Setters and Getters */

    public void setCacheSerializator(CacheSerializator<Object> cacheSerializator) {
        this.cacheSerializator = cacheSerializator;
    }
}