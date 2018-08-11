package ru.tandser.cache.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementation of serialization and deserialization of objects
 * based on the standard mechanism.
 *
 * @author Andrew Timokhin
 */
public class CacheSerializatorImpl implements CacheSerializator<Object> {

    private static final Logger logger = LoggerFactory.getLogger(CacheSerializatorImpl.class);

    private static final String DEFAULT_ROOT_STORAGE_PATH = "cache-storage";

    private Path rootStoragePath;

    public CacheSerializatorImpl(String rootStoragePath) throws IOException {
        this.rootStoragePath = rootStoragePath == null
                ? Paths.get(DEFAULT_ROOT_STORAGE_PATH)
                : Paths.get(rootStoragePath);

        if (!Files.isDirectory(this.rootStoragePath)) {
            Files.createDirectories(this.rootStoragePath);
        }
    }

    @Override
    public Path write(Object value) throws Exception {
        Path path = generatePath(Objects.requireNonNull(value));

        try (FileOutputStream   fos = new FileOutputStream(path.toFile());
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(value);
        }

        logger.debug("write: value = {}, path = {}", value, path);

        return path;
    }

    @Override
    public Object read(Path path) throws Exception {
        Objects.requireNonNull(path);

        Object value;

        try (FileInputStream   fis = new FileInputStream(path.toFile());
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            value = ois.readObject();
        }

        logger.debug("read: path = {}, value = {}", path, value);

        return value;
    }

    @Override
    public void remove(Path path) throws Exception {
        Files.deleteIfExists(path);

        logger.debug("remove: path = {}", path);
    }

    /* Service methods */

    private Path generatePath(Object value) {
        return rootStoragePath.resolve(UUID.randomUUID() + ".cache");
    }
}