package ru.tandser.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class L1CacheTest extends CacheTest {

    @Override
    @Autowired @Qualifier("l1Cache")
    public void setCache(Cache<Integer, Object> cache) {
        super.setCache(cache);
    }
}