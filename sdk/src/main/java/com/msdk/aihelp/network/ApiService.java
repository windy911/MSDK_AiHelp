package com.msdk.aihelp.network;

public class ApiService {

    private static final String API_PREFIX = "/api/v1";

    public static final String PATH_FAQ_SECTIONS = "/faq/sections";
    public static final String PATH_FAQ_ITEMS = "/faq/sections/%s/items";
    public static final String PATH_FAQ_DETAIL = "/faq/items/%s";
    public static final String PATH_FAQ_SEARCH = "/faq/search";
    public static final String PATH_FAQ_FEEDBACK = "/faq/items/%s/feedback";
    public static final String PATH_UPLOAD = "/upload";
    public static final String PATH_UNREAD_COUNT = "/chat/unread";
    public static final String PATH_CHAT_HISTORY = "/chat/history";

    public static String buildUrl(String domain, String path, String... args) {
        String formattedPath = (args.length > 0) ? String.format(path, (Object[]) args) : path;
        String base = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
        return base + API_PREFIX + formattedPath;
    }
}
