package com.msdk.aihelp.network;

import org.junit.Test;
import static org.junit.Assert.*;

public class ApiServiceTest {

    @Test
    public void getFAQSectionsUrl_constructsCorrectly() {
        String url = ApiService.buildUrl("https://cs.example.com", ApiService.PATH_FAQ_SECTIONS);
        assertEquals("https://cs.example.com/api/v1/faq/sections", url);
    }

    @Test
    public void getFAQItemsUrl_withSectionId_constructsCorrectly() {
        String url = ApiService.buildUrl("https://cs.example.com",
                ApiService.PATH_FAQ_ITEMS, "section_abc");
        assertEquals("https://cs.example.com/api/v1/faq/sections/section_abc/items", url);
    }

    @Test
    public void getSearchUrl_constructsCorrectly() {
        String url = ApiService.buildUrl("https://cs.example.com", ApiService.PATH_FAQ_SEARCH);
        assertEquals("https://cs.example.com/api/v1/faq/search", url);
    }

    @Test
    public void getUploadUrl_constructsCorrectly() {
        String url = ApiService.buildUrl("https://cs.example.com", ApiService.PATH_UPLOAD);
        assertEquals("https://cs.example.com/api/v1/upload", url);
    }

    @Test
    public void getUnreadCountUrl_constructsCorrectly() {
        String url = ApiService.buildUrl("https://cs.example.com", ApiService.PATH_UNREAD_COUNT);
        assertEquals("https://cs.example.com/api/v1/chat/unread", url);
    }
}
