package ru.tandser.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class L2CacheTest extends CacheTest {

    @Override
    @Autowired @Qualifier("l2Cache")
    public void setCache(Cache<Integer, Object> cache) {
        super.setCache(cache);
    }
}