package ru.tandser.cache.utils;

import ru.tandser.cache.Cache;
import ru.tandser.cache.CacheEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class CacheUtils {

    private CacheUtils() {}

    public static float round(float value, int scale) {
        return new BigDecimal(Float.toString(value)).setScale(scale, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    public static double median(Set<CacheEntity<?>> cache) {
        List<Integer> list = cache.stream()
                                  .map(CacheEntity::getCount)
                                  .collect(Collectors.toList());

        return median(list, Comparator.naturalOrder());
    }

    public static <T extends Number> double median(List<T> list, Comparator<T> comparator) {
        if (list.isEmpty()) {
            return 0;
        }

        int n = list.size() / 2;

        return list.size() % 2 == 0
                ? (nth(list, n - 1, comparator).doubleValue() + nth(list, n, comparator).doubleValue()) / 2.0
                :  nth(list, n,     comparator).doubleValue();
    }

    public static int roundMedian(Set<CacheEntity<?>> cache) {
        return (int) CacheUtils.median(cache);
    }

    public static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<>(
                (e1, e2) -> {
                    int res = e1.getValue().compareTo(e2.getValue());
                    return res != 0 ? res : 1;
                }
        );

        sortedEntries.addAll(map.entrySet());

        return sortedEntries;
    }

    public static <K, V> Map<K, LocalDateTime> getMapWithDateTimeCache(Cache<K, V> cache) {
        Map<K, LocalDateTime> result = new HashMap<>();

        for (Map.Entry<K, CacheEntity<?>> cacheEntry : cache.getCacheMap().entrySet()) {
            result.put(cacheEntry.getKey(), cacheEntry.getValue().getInitDateTime());
        }

        return result;
    }

    /* Service methods */

    private static <T> T nth(List<T> list, int n, Comparator<T> comparator) {
        T result, pivot;

        List<T> undPivot = new ArrayList<>();
        List<T> ovrPivot = new ArrayList<>();
        List<T> eqlPivot = new ArrayList<>();

        pivot = list.get(n / 2);

        for (T item : list) {
            int order = comparator.compare(item, pivot);

            if (order < 0) {
                undPivot.add(item);
            } else if (order > 0) {
                ovrPivot.add(item);
            } else {
                eqlPivot.add(item);
            }
        }

        if (n < undPivot.size()) {
            result = nth(undPivot, n, comparator);
        } else if (n < undPivot.size() + eqlPivot.size()) {
            result = pivot;
        } else {
            result = nth(ovrPivot, n - undPivot.size() - eqlPivot.size(), comparator);
        }

        return result;
    }
}