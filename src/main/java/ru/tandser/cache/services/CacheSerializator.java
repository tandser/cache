package ru.tandser.cache.services;

import java.nio.file.Path;

/**
 * Designed to implement the logic of serialization and
 * deserialization of objects.
 *
 * @author Andrew Timokhin
 */
public interface CacheSerializator<V> {

    Path write(V value) throws Exception;

    V read(Path path) throws Exception;

    void remove(Path path) throws Exception;
}