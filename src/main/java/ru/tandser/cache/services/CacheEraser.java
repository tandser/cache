package ru.tandser.cache.services;

import ru.tandser.cache.Cache;

/**
 * Designed to implement the logic of partial cache cleaning in
 * case of its overflow.
 *
 * @author Andrew Timokhin
 */
public interface CacheEraser<K, V> {

    void erase(Cache<K, V> cache, int cacheCapacity, double cacheLoadFactor);
}