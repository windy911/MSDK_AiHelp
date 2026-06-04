package com.msdk.aihelp.storage;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CacheManagerTest {

    private CacheManager cache;

    @Before
    public void setUp() {
        cache = new CacheManager();
    }

    @Test
    public void put_andGet_returnsValue() {
        cache.put("key1", "value1", 60000);
        assertEquals("value1", cache.get("key1"));
    }

    @Test
    public void get_expiredEntry_returnsNull() {
        cache.put("key1", "value1", 0);
        assertNull(cache.get("key1"));
    }

    @Test
    public void get_nonExistentKey_returnsNull() {
        assertNull(cache.get("missing"));
    }

    @Test
    public void clear_removesAllEntries() {
        cache.put("key1", "value1", 60000);
        cache.put("key2", "value2", 60000);
        cache.clear();
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));
    }
}
