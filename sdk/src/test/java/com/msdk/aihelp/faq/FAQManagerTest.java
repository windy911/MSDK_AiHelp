package com.msdk.aihelp.faq;

import com.msdk.aihelp.model.FAQItem;
import com.msdk.aihelp.model.FAQSection;
import com.msdk.aihelp.storage.CacheManager;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class FAQManagerTest {

    private CacheManager cacheManager;

    @Before
    public void setUp() {
        cacheManager = new CacheManager();
    }

    @Test
    public void cachedSections_returnsFromCache() {
        FAQSection section = new FAQSection();
        section.setSectionId("s1");
        section.setTitle("充值相关");
        List<FAQSection> sections = Arrays.asList(section);

        cacheManager.putFAQSections(sections);

        List<FAQSection> cached = cacheManager.get("faq_sections");
        assertNotNull(cached);
        assertEquals(1, cached.size());
        assertEquals("充值相关", cached.get(0).getTitle());
    }

    @Test
    public void cachedDetail_returnsFromCache() {
        FAQItem item = new FAQItem();
        item.setFaqId("f1");
        item.setQuestion("如何充值？");
        item.setAnswer("<p>通过商店充值</p>");

        cacheManager.putFAQDetail("f1", item);

        FAQItem cached = cacheManager.get("faq_detail_f1");
        assertNotNull(cached);
        assertEquals("如何充值？", cached.getQuestion());
    }
}
