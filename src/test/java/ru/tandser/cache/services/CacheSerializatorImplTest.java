package ru.tandser.cache.services;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import ru.tandser.cache.AbstractTest;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class CacheSerializatorImplTest extends AbstractTest {

    private static final String TEST_STRING = "test";

    @Autowired
    private CacheSerializator<Object> cacheSerializator;

    @Test
    public void write() throws Exception {
        Path path = cacheSerializator.write(TEST_STRING);

        assertNotNull(path);
        assertTrue(Files.isRegularFile(path));
    }

    @Test
    public void read() throws Exception {
        String mock = (String) cacheSerializator.read(ResourceUtils.getFile("classpath:mocks/test.cache").toPath());
        assertEquals(TEST_STRING, mock);
    }

    @Test
    public void remove() throws Exception {
        Path path = cacheSerializator.write(TEST_STRING);

        assertNotNull(path);
        assertTrue(Files.exists(path));
        assertTrue(Files.isRegularFile(path));

        cacheSerializator.remove(path);

        assertFalse(Files.exists(path));
    }
}