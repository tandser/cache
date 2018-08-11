package ru.tandser.cache;

import com.google.common.base.MoreObjects;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A wrapper for a cached object. There is a counter for calling an
 * object from the cache and the creation date.
 *
 * @author Andrew Timokhin
 */
public class CacheEntity<V> {

    private LocalDateTime initDateTime;
    private AtomicInteger count;
    private V             value;

    public CacheEntity(V value) {
        this.initDateTime = LocalDateTime.now();
        this.count        = new AtomicInteger(0);
        this.value        = Objects.requireNonNull(value);
    }

    public int incrementCount() {
        return count.incrementAndGet();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        CacheEntity<?> that = (CacheEntity<?>) obj;

        return this.initDateTime.isEqual(that.initDateTime) &&
               this.count == that.count                     &&
               Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(initDateTime, count, value);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("initDateTime", initDateTime)
                .add("count",        count)
                .add("value",        value)
                .toString();
    }

    /* Setters and Getters */

    public LocalDateTime getInitDateTime() {
        return initDateTime;
    }

    public int getCount() {
        return count.get();
    }

    public V getValue() {
        return value;
    }
}