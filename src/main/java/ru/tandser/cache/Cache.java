package ru.tandser.cache;

import java.util.Map;
import java.util.Set;

/**
 * Designed to implement a cache of any level. As a key, it is
 * recommended to use a unique value characterizing the object, for
 * example, its hash code.
 *
 * @author Andrew Timokhin
 */
public interface Cache<K, V> {

    Set<CacheEntity<?>> getCacheSet();

    Map<K, CacheEntity<?>> getCacheMap();

    void put(K key, V value);

    V get(K key);

    void remove(K key);

    boolean contains(K key);

    int size();

    void clear();
}