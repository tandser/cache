package ru.tandser.cache;

import java.util.*;

public class TestUtils {

    private TestUtils() {}

    public static Map<Integer, Integer> gettingCache() {
        Map<Integer, Integer> result = new HashMap<>();

        result.put(0,  3);
        result.put(1,  1);
        result.put(2,  1);
        result.put(3,  2);
        result.put(4,  3);
        result.put(5,  0);
        result.put(6,  4);
        result.put(7,  1);
        result.put(8,  2);
        result.put(9,  4);
        result.put(10, 0);
        result.put(11, 2);
        result.put(12, 0);
        result.put(13, 3);
        result.put(14, 1);
        result.put(15, 2);
        result.put(16, 1);
        result.put(17, 3);
        result.put(18, 3);
        result.put(19, 4);
        result.put(20, 5);
        result.put(21, 4);
        result.put(22, 1);
        result.put(23, 1);
        result.put(24, 4);

        return result;
    }

    public static Set<Integer> keysL1CacheShift1() {
        return new HashSet<>(Arrays.asList(0, 3, 4, 6, 8, 9, 11, 13, 15, 17, 18, 19, 20, 21));
    }

    public static Set<Integer> keysL2CacheShift1() {
        return new HashSet<>(Arrays.asList(1, 2, 5, 7, 10, 12, 14, 16, 22, 23, 24));
    }

    public static Set<Integer> keysL1CacheShift2() {
        return new HashSet<>(Arrays.asList(0, 4, 6, 9, 13, 17, 18, 19, 20, 21, 24));
    }

    public static Set<Integer> keysL2CacheShift2() {
        return new HashSet<>(Arrays.asList(1, 2, 3, 5, 7, 8, 10, 11, 12, 14, 15, 16, 22, 23));
    }
}