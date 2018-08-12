package ru.tandser.cache;

import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import ru.tandser.cache.services.CacheEraser;
import ru.tandser.cache.utils.CacheUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of a two-level cache. The L1-cache has data in
 * memory, the L2-cache in the file system.
 *
 * @author Andrew Timokhin
 */
public class TwoLevelCache implements MultilevelCache<Integer, Object> {

    private static final Logger logger = LoggerFactory.getLogger(TwoLevelCache.class);

    private static final int DEFAULT_NUM_OF_EXTRACTS_FOR_SHIFT = 25;
    private static final int DEFAULT_TIME_LIMIT                = 60;

    private static final int DEFAULT_L1_CACHE_CAPACITY = 100;
    private static final int DEFAULT_L2_CACHE_CAPACITY = 100;

    private static final float DEFAULT_L1_CACHE_LOAD_FACTOR = 0.75f;
    private static final float DEFAULT_L2_CACHE_LOAD_FACTOR = 0.75f;

    private static final int DEFAULT_MIN_THRESHOLD_CALLS_FOR_SHIFT = 3;

    private Cache<Integer, Object>       l1Cache;
    private Cache<Integer, Object>       l2Cache;
    private CacheEraser<Integer, Object> cacheEraser;

    private int l1CacheCapacity;
    private int l2CacheCapacity;

    private float l1CacheLoadFactor;
    private float l2CacheLoadFactor;

    private int numOfExtractsForShift;
    private int timeLimit;
    private int minThresholdCallsForShift;

    private AtomicInteger cacheGetCounter = new AtomicInteger(0);

    private final ReentrantLock lock = new ReentrantLock();

    {
        numOfExtractsForShift     = DEFAULT_NUM_OF_EXTRACTS_FOR_SHIFT;
        timeLimit                 = DEFAULT_TIME_LIMIT;
        minThresholdCallsForShift = DEFAULT_MIN_THRESHOLD_CALLS_FOR_SHIFT ;
        l1CacheCapacity           = DEFAULT_L1_CACHE_CAPACITY;
        l2CacheLoadFactor         = DEFAULT_L2_CACHE_CAPACITY;
        l1CacheLoadFactor         = DEFAULT_L1_CACHE_LOAD_FACTOR;
        l2CacheLoadFactor         = DEFAULT_L2_CACHE_LOAD_FACTOR;
    }

    @Override
    public Set<CacheEntity<?>> getCacheSet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Integer, CacheEntity<?>> getCacheMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(Integer key, Object value) {
        requireNonNull(key);
        requireNonNull(value);

        logger.debug("put: key = {}, value = {}", key, value);

        l1Cache.put(key, value);

        if (l1Cache.size() >= l1CacheCapacity) {
            shift();
        }
    }

    @Override
    public Object get(Integer key) {
        requireNonNull(key);

        Object result;

        if (l1Cache.contains(key)) {
            result = get(l1Cache, key);
            logger.debug("get: result from L1-cache = {}", result);
            return result;
        }

        if (l2Cache.contains(key)) {
            result = get(l2Cache, key);
            logger.debug("get: result from L2-cache = {}", result);
            return result;
        }

        return null;
    }

    @Override
    public void remove(Integer key) {
        logger.debug("remove: key = {}", key);

        if (l1Cache.contains(requireNonNull(key))) {
            l1Cache.remove(key);
        }

        if (l2Cache.contains(key)) {
            l2Cache.remove(key);
        }
    }

    @Override
    public boolean contains(Integer key) {
        logger.debug("contains: key = {}", key);
        return l1Cache.contains(requireNonNull(key)) || l2Cache.contains(key);
    }

    @Override
    public int size() {
        int l1CacheSize = l1Cache.size();
        int l2CacheSize = l2Cache.size();

        int result = l1CacheSize + l2CacheSize;

        logger.debug("size: L1-cache size = {}, L2-cache size = {}, result = {}", l1CacheSize, l2CacheSize, result);

        return result;
    }

    @Override
    public void clear() {
        cacheGetCounter.set(0);
        l1Cache.clear();
        l2Cache.clear();
    }

    @Override
    @Async
    public void shift() {
        logger.debug("shift: attempt to get a lock");

        if (lock.tryLock()) {
            logger.debug("shift: a lock received");

            try {
                cacheGetCounter.set(0);

                Map<Integer, CacheEntity<?>> shiftCalledL1Cache = getShiftCalledL1Cache();

                // L1Cache -> L2Cache
                for (Integer key : shiftCalledL1Cache.keySet()) {
                    l2Cache.put(key, l1Cache.get(key));
                    l1Cache.remove(key);
                }

                Map<Integer, CacheEntity<?>> shiftCalledL2Cache = getShiftCalledL2Cache();

                // L1Cache <- L2Cache
                for (Integer key : shiftCalledL2Cache.keySet()) {
                    if (!shiftCalledL1Cache.containsKey(key)) {
                        l1Cache.put(key, l2Cache.get(key));
                        l2Cache.remove(key);
                    }
                }

                if (l1Cache.size() >= l1CacheCapacity) {
                    cacheEraser.erase(l1Cache, l1CacheCapacity, l1CacheLoadFactor);
                }

                if (l2Cache.size() >= l2CacheCapacity) {
                    cacheEraser.erase(l2Cache, l2CacheCapacity, l2CacheLoadFactor);
                }
            } finally {
                lock.unlock();
            }
        } else {
            logger.debug("shift: a lock not received");
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("l1Cache", l1Cache)
                .add("l2Cache", l2Cache)
                .toString();
    }

    /* Service methods */

    private Object get(Cache<Integer, Object> cache, Integer key) {
        Object result = cache.get(key);

        if (cacheGetCounter.get() >= numOfExtractsForShift) {
            shift();
        } else {
            cacheGetCounter.incrementAndGet();
        }

        return result;
    }

    private Map<Integer, CacheEntity<?>> getShiftCalledL1Cache() {
        Map<Integer, CacheEntity<?>> result = new HashMap<>();

        int roundMedian = CacheUtils.roundMedian(l1Cache.getCacheSet());

        for (Map.Entry<Integer, CacheEntity<?>> cacheEntry : l1Cache.getCacheMap().entrySet()) {
            int numOfCalls = cacheEntry.getValue().getCount();

            if (numOfCalls < roundMedian && cacheEntry.getValue().getInitDateTime().isBefore(LocalDateTime.now().minusSeconds(timeLimit))) {
                result.put(cacheEntry.getKey(), cacheEntry.getValue());
            }
        }

        logger.debug("getShiftCalledL1Cache: roundMedian = {}, l1Cache = {}, result = {}", roundMedian, l1Cache, result);

        return result;
    }

    private Map<Integer, CacheEntity<?>> getShiftCalledL2Cache() {
        Map<Integer, CacheEntity<?>> result = new HashMap<>();

        int roundMedian = CacheUtils.roundMedian(l2Cache.getCacheSet());

        for (Map.Entry<Integer, CacheEntity<?>> cacheEntry : l2Cache.getCacheMap().entrySet()) {
            int numOfCalls = cacheEntry.getValue().getCount();

            if (roundMedian < minThresholdCallsForShift && numOfCalls >= minThresholdCallsForShift) {
                result.put(cacheEntry.getKey(), cacheEntry.getValue());
            } else if (roundMedian >= minThresholdCallsForShift && numOfCalls >= roundMedian) {
                result.put(cacheEntry.getKey(), cacheEntry.getValue());
            }
        }

        logger.debug("getShiftCalledL2Cache: roundMedian = {}, l2Cache = {}, result = {}", roundMedian, l2Cache, result);

        return result;
    }

    private float roundCacheFactor(float cacheLoadFactor) {
        float roundCacheLoadFactor = CacheUtils.round(cacheLoadFactor, 2);

        if (roundCacheLoadFactor < 0 || roundCacheLoadFactor > 1) {
            throw new IllegalArgumentException("The load factor must be greater than 0 and less than or equal to 1");
        }

        return roundCacheLoadFactor;
    }

    /* Setters and Getters */

    public Cache<Integer, Object> getL1Cache() {
        return l1Cache;
    }

    public void setL1Cache(Cache<Integer, Object> l1Cache) {
        this.l1Cache = l1Cache;
    }

    public Cache<Integer, Object> getL2Cache() {
        return l2Cache;
    }

    public void setL2Cache(Cache<Integer, Object> l2Cache) {
        this.l2Cache = l2Cache;
    }

    public void setCacheEraser(CacheEraser<Integer, Object> cacheEraser) {
        this.cacheEraser = cacheEraser;
    }

    public void setL1CacheCapacity(int l1CacheCapacity) {
        this.l1CacheCapacity = l1CacheCapacity;
    }

    public void setL2CacheCapacity(int l2CacheCapacity) {
        this.l2CacheCapacity = l2CacheCapacity;
    }

    public void setL1CacheLoadFactor(float l1CacheLoadFactor) {
        this.l1CacheLoadFactor = roundCacheFactor(l1CacheLoadFactor);
    }

    public void setL2CacheLoadFactor(float l2CacheLoadFactor) {
        this.l2CacheLoadFactor = roundCacheFactor(l2CacheLoadFactor);
    }

    public void setNumOfExtractsForShift(int numOfExtractsForShift) {
        this.numOfExtractsForShift = numOfExtractsForShift;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public void setMinThresholdCallsForShift(int minThresholdCallsForShift) {
        this.minThresholdCallsForShift = minThresholdCallsForShift;
    }
}