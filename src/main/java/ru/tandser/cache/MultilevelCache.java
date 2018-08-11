package ru.tandser.cache;

/**
 * Designed to implement a multi-level cache. Extends the
 * single-level cache interface with an additional method of moving
 * data between levels.
 *
 * @autor Andrew Timokhin
 */
public interface MultilevelCache<K, V> extends Cache<K, V> {

    void shift();
}