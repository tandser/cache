package ru.tandser.cache.utils;

import org.junit.Test;
import ru.tandser.cache.AbstractTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CacheUtilsTest extends AbstractTest {

    private static final double DELTA = 1E-6f;

    @Test
    public void round() {
        assertEquals(0.10f,  CacheUtils.round(0.100f, 2),  DELTA);
        assertEquals(0.10f,  CacheUtils.round(0.101f, 2),  DELTA);
        assertEquals(0.10f,  CacheUtils.round(0.102f, 2),  DELTA);
        assertEquals(0.10f,  CacheUtils.round(0.103f, 2),  DELTA);
        assertEquals(0.10f,  CacheUtils.round(0.104f, 2),  DELTA);

        assertEquals(0.11f,  CacheUtils.round(0.105f, 2),  DELTA);
        assertEquals(0.11f,  CacheUtils.round(0.106f, 2),  DELTA);
        assertEquals(0.11f,  CacheUtils.round(0.107f, 2),  DELTA);
        assertEquals(0.11f,  CacheUtils.round(0.108f, 2),  DELTA);
        assertEquals(0.11f,  CacheUtils.round(0.109f, 2),  DELTA);

        assertEquals(-0.10f, CacheUtils.round(-0.100f, 2), DELTA);
        assertEquals(-0.10f, CacheUtils.round(-0.101f, 2), DELTA);
        assertEquals(-0.10f, CacheUtils.round(-0.102f, 2), DELTA);
        assertEquals(-0.10f, CacheUtils.round(-0.103f, 2), DELTA);
        assertEquals(-0.10f, CacheUtils.round(-0.104f, 2), DELTA);

        assertEquals(-0.11f, CacheUtils.round(-0.105f, 2), DELTA);
        assertEquals(-0.11f, CacheUtils.round(-0.106f, 2), DELTA);
        assertEquals(-0.11f, CacheUtils.round(-0.107f, 2), DELTA);
        assertEquals(-0.11f, CacheUtils.round(-0.108f, 2), DELTA);
        assertEquals(-0.11f, CacheUtils.round(-0.109f, 2), DELTA);
    }

    @Test
    public void median() {
        List<Integer> ints = new ArrayList<>(Arrays.asList(24, 26, 2, 37, 51, 1, 26, 3, 88, 84, 62, 84, 32, 74, 70, 80, 9, 6, 69, 16, 75, 81, 5, 21, 50, 42, 86, 58, 37, 54, 92, 64, 74, 50, 97, 50, 33, 67, 5, 7, 53, 30, 21, 95, 40, 77, 74, 77, 33, 72, 27, 91, 35, 80, 52, 29, 88, 24, 21, 58, 1, 94, 57, 30, 57, 88, 82, 12, 70, 5, 70, 51, 4, 19, 6, 58, 79, 43, 67, 14, 17, 59, 51, 9, 33, 22, 35, 97, 65, 36, 10, 71, 45, 71, 92, 18, 63, 72, 17, 84));

        assertEquals(51, CacheUtils.median(ints, Comparator.naturalOrder()), DELTA);
    }
}