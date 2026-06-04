package com.msdk.aihelp.storage;

import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {

    private static final long FAQ_SECTIONS_TTL = 30 * 60 * 1000L;
    private static final long FAQ_DETAIL_TTL = 60 * 60 * 1000L;

    private final ConcurrentHashMap<String, CacheEntry> store = new ConcurrentHashMap<>();

    public void put(String key, Object value, long ttlMs) {
        store.put(key, new CacheEntry(value, System.currentTimeMillis() + ttlMs));
    }

    public void putFAQSections(Object value) {
        put("faq_sections", value, FAQ_SECTIONS_TTL);
    }

    public void putFAQDetail(String faqId, Object value) {
        put("faq_detail_" + faqId, value, FAQ_DETAIL_TTL);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry entry = store.get(key);
        if (entry == null) return null;
        if (System.currentTimeMillis() > entry.expireAt) {
            store.remove(key);
            return null;
        }
        return (T) entry.value;
    }

    public void clear() {
        store.clear();
    }

    private static class CacheEntry {
        final Object value;
        final long expireAt;

        CacheEntry(Object value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }
    }
}
